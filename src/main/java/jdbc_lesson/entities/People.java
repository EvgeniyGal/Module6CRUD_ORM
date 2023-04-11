package jdbc_lesson.entities;

import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class People implements BaseEntity<BigInteger>{
    
    private BigInteger id;
    
    private Integer age;
    
    private String name;
    
    private String birthday;
    
}
