package study.datajpa.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item implements Persistable<String> {

    /**
     * custom id 로 직접할당할 경우 persistable 구
     */
    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createDate;

    public Item(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * @CreateDate 는 persist 직전에 실행 isNew 까지는 실행 안되서 null
     */
    @Override
    public boolean isNew() {
        return createDate == null;
    }
}
