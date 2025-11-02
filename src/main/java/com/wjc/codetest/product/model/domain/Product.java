package com.wjc.codetest.product.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
/* - 문제 : entity class에 setter를 사용하고 있어 의도를 파악하기 어렵습니다.
   - 원인 : product 수정을 실행 할 때 setter를 사용하고 있습니다.
   - 개선안(대안) :
   public void modifyCategory(String category) {
     this.category = category;
   }
   public void modifyName(String name) {
     this.name = name;
   }
   으로 수정되어야 하고, category, name 등 수정하는 파라미터 검증 메소드도 필요 합니다.(null 체크, 글자 수 제한 등)

   - 선택 근거 :
   메서드 이름으로 의도를 표현할 수 있습니다.
   비즈니스 요구사항을 해결할 수 있고 사용자의 입력이 올바른지 확인할 수 있습니다.
 */
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "category")
    private String category;
    /*
    -문제 : 사실상 category 종류가 무한으로 증식 가능, 중복 가능
    -원인 : category type이 String 입니다.
    -개선안 :
    Category Entity를 만들어 관리하거나 Constants 패키지에 Enum 으로 만들어 관리할 수 있도록 합니다.
    -선택 근거 :
    product에서 category는 중요한 역할을 합니다.
    category를 이용한 조회 등 여러 곳에서 사용할 속성이지만, 단순 문자열이라면 "전자제품", "전자 제품" 처럼 중복이 발생할 수 있습니다.
     */

    @Column(name = "name")
    private String name;

    protected Product() {
    }

    public Product(String category, String name) {
        this.category = category;
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }
    /*
    - 문제 : 메서드 중복
    - 원인 : entity class에 @Getter가 존재 하지만, 같은 기능을 하는 getCategory, getName 메소드가 중복으로 있습니다.
    - 개선안(대안) : get method 삭제
    - 선택 근거 :
    이름과 의미가 완전히 똑같은 메서드를 삭제함으로써 가독성이 좋아집니다.
     */
}
