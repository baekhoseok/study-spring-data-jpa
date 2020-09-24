package study.datajpa.repositiry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.datajpa.entity.Item;

@SpringBootTest
public class ItemRepositoryTest {
    
    @Autowired ItemRepository itemRepository;
    
    @Test
    public void saveTest() throws Exception {
        //given
        Item item = new Item("A");
        itemRepository.save(item);

        //when
        
        //then
    }
}
