# #JPA/상속관계매핑

## 상속 관계 매핑

- 관계형 데이터베이스는 상속 관계 X
- 슈퍼타입 서브타입 관계라는 모델링 기법이 객체 상속과 유사
- 상속관계 매핑 : 객체의 상속 구조와 DB의 슈퍼타입 서브타입 관계를 매핑


[image:1278D2FA-08C4-4912-90E4-C6DC1DF56B8F-83586-0002A4D6873CDE9A/스크린샷 2020-02-12 오후 8.58.24.png]

- 슈퍼타입 서브타입 논리 모델을 실제 물리 모델로 구현하는 방법

상속 관계 매핑이란 DB의 슈퍼타입 서브타인 논리 모델로 구체화해서 매핑함 

##### 조인전략 (각각 테이블로 변환)

[image:3F2C032D-F8AA-43F6-8A54-1B237CC38CD3-83586-0002A4FAC1C1CC11/스크린샷 2020-02-12 오후 9.00.59.png]

```java
// 상위(부모) 객체에 @Inheritance 사용하면 됨
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn() // DType을 넣어주는 것이 좋다 Item 테이블로 접근해도 가져올수 있도록
public class Item {

    @Id @GeneratedValue
    private Long id;

    private String name;
    private int price;
}

// Movie
@Entity
@DiscriminatorValue(“M”) // DType 축약어
public class Movie extends Item {
    private String director;
    private String actor;
	  … // 생략
}

// JpaMain
Movie movie = new Movie();
movie.setDirector(“aaaa”);
movie.setActor(“bbb”);
movie.setName(“바람과함께사라지다”);
movie.setPrice(10000);

em.persist(movie);
```

[image:CA6C9174-3CC4-4789-A864-084840AA8DF7-83586-0002A7E8DC4FB44C/스크린샷 2020-02-12 오후 9.54.37.png]

- 장점 
	- 테이블 정규화 
	- 외래 키 참조 무결성 제약조건 활용가능 (부모 테이블만 봐도 됨)
	- 저장공간 효율화 
* 단점
	* 조회시조인을많이사용,성능저하 
	* 조회 쿼리가 복잡함
	* 데이터 저장시 INSERT SQL 2번 호출 (크게 영향 없음)

-> 기본적으로 정석임


##### 단일테이블 전략 (통합테이블로 변환)

[image:EB123246-85AD-449D-8A00-96F7717E4726-83586-0002A516BAD8E42C/스크린샷 2020-02-12 오후 9.02.59.png]

`@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`
-> *@DiscriminatorColumn* 이 없어도 DTYPE이 생긴다
그리고 @Inheritance 가 없어도 기본으로 단일테이블 전략을 채택한다
(h2는 기본적으로 단일테이블 전략으로 쿼리가 나감 (테이블 하나))

[image:3F8979F8-841A-4198-AE1E-1E65EA49FB47-83586-0002A803E4FE22F9/스크린샷 2020-02-12 오후 9.56.33.png]
-> 성능상으로 가장 좋음

- 장점 
	* 조인이 필요 없으므로 일반적으로 조회 성능이 빠름 
	* 조회 쿼리가 단순함 
* 단점 
	- 자식 엔티티가 매핑한 컬럼은 모두 null 허용 
	* 단일테이블에모든것을저장하므로테이블이커질수있다. 상황에 따라서 조회 성능이 오히려 느려질 수 있다. 


##### 구현 클래스 마다 테이블 전략 (서브타입 테이블로 변환)

[image:1D9F1A11-8989-460F-8D42-CCA8C56512CF-83586-0002A51EE316C112/스크린샷 2020-02-12 오후 9.03.35.png] 

`*@Inheritance*(strategy = InheritanceType.TABLE_PER_CLASS)`
-> Item 테이블은 abstract 클래스여야 함
넣을때는 괜찮은데 가져올때 union all 쿼리로 다 뒤져보기 때문에 비효율적이다

[image:F9A2B7F0-8E88-40C0-98D5-A1A94C686EAB-83586-0002A8B8E00A11B7/스크린샷 2020-02-12 오후 10.09.33.png]

- **이 전략은 데이터베이스 설계자와****ORM****전문가 둘 다 추천****X**



---
## @MappedSuperclass

실무에서 자주 쓰임

- 공통 매핑 정보가 필요할 때 사용 (id, name)

[image:E640A89B-51B6-4845-A149-C0D530D39CE8-83586-0002A9C62BB06DE9/스크린샷 2020-02-12 오후 10.29.42.png]


- 상속관계 매핑 X
- 엔티티 X, 테이블과 매핑 X
- 부모 클래스를 상속 받는 자식 클래에 매핑 정보만 제공
- 조회, 검색 불가 (em.find(BaseEntity) X)
- 직접 생성해서 사용할 일이 없으므로 추상 클래스 권장
- 테이블과 관계가 없고 단순히 엔티티가 공통으로 사용하는 매핑 정보를 모으는 역할
- 주로 등록일, 수정일, 등록자, 수정자 같은 전체 엔티티에서 공통으로 적용하는 정보를 모을 때 사용
- 참고: @Entity클래스는 엔티티나 @MappedSuperclass로 지정한 클래스만 상속 가능


```java
@MappedSuperclass
public class BaseEntity {
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
	  … // 이하 생략
}
```
-> BaseEntity를 Item 클래스가 상속하면 조인 전략에서는 Item 테이블에만 생성이된다.


실무에서 상속관계를 쓰지 않는 것은 아니지만 데이터의 양이 늘어남에 따라 테이블이 많은 것이 큰 부담이 될 수 있기 때문에 하위 클래스의 데이터를 json으로 말아 넣는 경우가 있기도 함. 
-> 경우에 따라 다른데 trade-off 를 잘 따져봐야 함




