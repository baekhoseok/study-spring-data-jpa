package study.datajpa.repositiry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @PersistenceContext
    private EntityManager em;

    @Test
    public void findByUsernameAndAgeGreaterThan() throws Exception {
        //given
        Member member1 = new Member("aaa", 10);
        Member member2 = new Member("aaa", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<Member> members = memberRepository.findByUsernameAndAgeGreaterThan("aaa", 15);

        //then
        assertThat(members.get(0).getUsername()).isEqualTo("aaa");
        assertThat(members.get(0).getAge()).isEqualTo(20);
        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    public void findUserTest() throws Exception {
        //given
        Member member1 = new Member("aaa", 10);
        Member member2 = new Member("bbb", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<Member> result = memberRepository.findUser("aaa", 10);

        //then
        assertThat(result.get(0)).isEqualTo(member1);

    }

    @Test
    public void memberDtoTest() throws Exception {
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("aaa", 10);
        Member member2 = new Member("bbb", 20);
        member1.changeTeam(teamA);
        member2.changeTeam(teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<MemberDto> memberDtos = memberRepository.findMemberDto();

        //then
        memberDtos.forEach(m -> System.out.println(m.toString()));
    }

    @Test
    public void findByNamesTest() throws Exception {
        //given
        Member member1 = new Member("aaa", 10);
        Member member2 = new Member("bbb", 20);
        Member member3 = new Member("ccc", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        //when
        List<Member> members = memberRepository.findByNames(Arrays.asList("aaa", "ddd", "ccc"));

        //then
        for (Member member : members) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void paging_test() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        /**
         * Entity 가 아니므로 외부에 리턴해도
         */
        Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        //then
        List<Member> contents = page.getContent();

        assertThat(contents.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

    }

    @Test
    public void bulkUpdateTest() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        //when
        /**
         * bulk update 시 db에는 적용되나 영속성 컨텍스트에서는 적용이 되지 않는다
         * 따라서 bulk update 후 영속성 컨텍스트를 초기화 해줘야 데이터 무결성이 해결된다.
         */
        int resultCount = memberRepository.bulkAgePlus(20);
//        em.flush();
//        em.clear();


        List<Member> result = memberRepository.findByUsername("member5");
        Member member = result.get(0);

        //then
        assertThat(resultCount).isEqualTo(3);
        assertThat(member.getAge()).isEqualTo(41);
    }

    @Test
    public void findMemberLazyTest() throws Exception {
        //given
        Team teamA = teamRepository.save(new Team("teamA"));
        Team teamB = teamRepository.save(new Team("teamB"));
        em.persist(teamA);
        em.persist(teamB);
        Team teamC = teamRepository.save(new Team("teamC"));
        Team teamD = teamRepository.save(new Team("teamD"));
        em.persist(teamD);
        em.persist(teamC);

        Member member1 = memberRepository.save(new Member("member1", 10, teamA));
        Member member2 = memberRepository.save(new Member("member2", 20, teamB));
        em.persist(member1);
        em.persist(member2);
        Member member3 = memberRepository.save(new Member("member3", 30, teamC));
        Member member4 = memberRepository.save(new Member("member4", 40, teamD));
        em.persist(member3);
        em.persist(member4);


        em.flush();
        em.clear();

        //when
        List<Member> members = memberRepository.findAll();

        //then
        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("team.class = " + member.getTeam().getClass());
            System.out.println("team = " + member.getTeam());
        }
    }

    @Test
    public void queryHint() throws Exception {
        //given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
        Member findMember = memberRepository.findReadOnlyByUsername(member1.getUsername());
        findMember.setUsername("member2");

        em.flush();
        //then
    }

    @Test
    public void lockTest() throws Exception {
        //given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
        List<Member> result = memberRepository.findLockByUsername(member1.getUsername());

        //then

    }

    @Test
    public void customTest() throws Exception {
        //given
        List<Member> memberCustom = memberRepository.findMemberCustom();
        //when

        //then
    }

    @Test
    public void jpaBaseEntityTest() throws Exception {
        //given
        Member member1 = memberRepository.save(new Member("member1", 10));

        Thread.sleep(100);
        member1.setUsername("member2");
        em.flush();
        em.clear();

        //when
        Member findMember = memberRepository.findById(member1.getId()).get();

        //then
        System.out.println("findMember.getCreateDate = " + findMember.getCreateDate());
        System.out.println("findMember.getLastModifiedDate = " + findMember.getLastModifiedDate());
//        System.out.println("findMember.getCreatedBy = " + findMember.getCreatedBy());
//        System.out.println("findMember.getLastModifiedBy = " + findMember.getLastModifiedBy());
    }
}
