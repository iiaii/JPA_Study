# #JPA/값타입


## 기본값 타입

JPA 에는 크게 2가지 타입 존재

- 엔티티 타입
	- @Entity 로 정의하는 객체
	- 데이터가 변해도 식별자로 지속해서 추적 가능
	- 예) 회원 **Order -> OrderItem**을 영속성 전이 ALL 설정 
- 값타입
	- int, Integer, String처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체 
	- 식별자가 없고 값만 있으므로 변경시 추적 불가 
	- 예) 숫자 100을 200으로 변경하면 완전히 다른 값으로 대체 


여기서 값타입은 3가지로 분류

- 기본값 타입
	- 자바 기본타입 (int, double)
	- 래퍼 클래스 (Integer, Long)
	- String
- 임베디드 타입 (embedded type, 복합 값 타입)
- 컬렉션 값 타입 (collection value type)


##### 기본값 타입

- 예) String name, int age
- **생명주기는 엔티티에 의존적**
	- 회원을 삭제하면 이름, 나이 필드도 함께 삭제
- 값 타입은 공유하면 안된다
	- 회원이름을 변경했는데 다른 회원 이름도 변경되어서는 안됨

- int, double 같은 기본 타입(primitive type)은 절대 공유해서는 안됨
- 기본값 타입은 항상 값을 복사한다
- Integer 같은 래퍼 클래스나 String 같은 특수한 클래스는 공유 가능 객체이지만, 변경이 안된다


---
## 임베디드 타입

[image:51325892-E616-49CA-9FC8-DADD08602C08-83586-0002E4EC64E4FFF2/page15image50466672.png] 

- 재사용
- 높은 응집도
- Period.isWork()처럼 해당 값 타입만 사용하는 의미 있는 메소 드를 만들 수 있음 
- 임베디드 타입을 포함한 모든 값 타입은, 값 타입을 소유한 엔티 티에 생명주기를 의존함 


##### 임베디드 타입과 테이블 매핑

- 임베디드 타입은 엔티티의 값일 뿐이다
- 임베디드 타입을 사용하기 전과 후에 **매핑하는 테이블은 같다**
- 객체와 테이블을 아주 세밀하게(find-grained) 매핑하는 것이 가능 
- 잘 설계한 ORM 애플리케이션은 매핑한 테이블의 수보다 클래스의 수가 더 많음 


```java
// 임베디드 타입 (기본생성자는 필수)
// @Embeddable : 값 타입을 정의하는 곳에 표시
// @Embedded : 값 타입을 사용하는 곳에 표시
@Embeddable
public class Period {
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Period() {
    }

    public Period(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
	  … // 생략
}

// Member
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = “MEMBER_ID”)
    private Long id;

    @Column(name = “USERNAME”)
    private String username;

    // 기간
    @Embedded
    private Period workPeriod;

    // 주소
    @Embedded
    private Address homeAddress;

	  // @AttributeOverrides 속성 재정의
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = “city”, column = @Column(name = “company_city”)),
            @AttributeOverride(name = “street”, column = @Column(name = “company_street”)),
            @AttributeOverride(name = “zipcode”, column = @Column(name = “company_zipcode”))
    })
    private Address companyAddress;
	  … // 생략
}
```

참고 : 임베디드 타입의 값이 null이면 매핑한 컬럼 값은 모두 null 


---
## 값 타입과 불변 객체

값 타입은 복잡한 객체 세상을 단순화 하기위해 만든 개념. 따라서 값 타입은 단순하고 안전하게 다룰 수 있어야 한다.

- 항상 값을 복사해서 사용하면 공유 참조로 인해 발생하는 부작용 을 피할 수 있다. 
- 문제는 임베디드 타입처럼 **직접 정의한 값 타입은 자바의 기본 타입이 아니라 객체 타입**이다. 
- 자바 기본 타입에 값을 대입하면 값을 복사한다.
- **객체 타입은 참조 값을 직접 대입하는 것을 막을 방법이 없다****.****객체의 공유 참조는 피할 수 없다****.**

-> 객체 타입은 call by reference 라서 값의 공유가 가능하다

##### 불변객체

- 객체 타입을 수정할 수 없게 만들면 부작용을 원천 차단
- 값 타입은 불변 객체(immutable object)로 설계해야함 
- 불변 객체 : 생성 시점 이후 절대 값을 변경할 수 없는 객체
* 생성자로만 값을 설정하고 수정자(Setter)를 만들지 않으면 됨 
* 참고: Integer, String은 자바가 제공하는 대표적인 불변 객체 

-> 임베디드 값 타입은 무조건 불변객체로 만들어서 사용해야 한다


---
## 값 타입 비교

값 타입이기 때문에 인스턴스가 다르더라도 안의 값이 같으면 같은것으로 봐야 한다

```java
Address a = new Address(“서울시”);
Address b = new Address(“서울시”);

a == b // false
a.equals(b) // false
```

- **동일성(identity)비교**: 인스턴스의 참조 값을 비교, == 사용 
- **동등성(equivalence)비교**: 인스턴스의 값을 비교, equals() 사용
- 값 타입은 a.equals(b)를 사용해서 동등성 비교를 해야 함 
- 값 타입의 equals() 메소드를 적절하게 재정의(주로 모든 필드 사용) 


```java
// 임베디드 값 타입 class 에서 equals 와 hashCode 재정의
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Address address = (Address) o;
    return Objects.equals(city, address.city) &&
            Objects.equals(street, address.street) &&
            Objects.equals(zipcode, address.zipcode);
}

@Override
public int hashCode() {
    return Objects.hash(city, street, zipcode);
}
```
-> IDE 에서 자동완성 해주는 것으로 사용하는 것이 안전


---
## 값 타입 컬렉션

값 타입을 컬렉션으로 가지는 경우를 표현
[image:068AEA3E-F05F-4BD3-992C-C5EAFBCF1511-16850-0003619ADEA8354D/스크린샷 2020-03-05 오후 10.19.25.png]

- 값 타입을 하나 이상 저장할 때 사용
- @ElementCollection, @CollectionTable 
- 데이터베이스는 컬렉션을 같은 테이블에 저장할 수 없다
- 컬렉션을 저장하기 위한 별도의 테이블이 필요함

```java
// Member
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = “MEMBER_ID”)
    private Long id;

    @Column(name = “USERNAME”)
    private String username;

    @Embedded
    private Address homeAddress;

    @ElementCollection
    @CollectionTable(name = “FAVORITE_FOOD”, joinColumns = @JoinColumn(name = “MEMBER_ID”))
    @Column(name = “FOOD_NAME”)
    private Set<String> favoriteFoods = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = “ADDRESS”, joinColumns = @JoinColumn(name = “MEMBER_ID”))
    private List<Address> addressHistory = new ArrayList<>();
	  … // 생략
}

/* ============================================= */
// JpaMain 저장코드
Member member = new Member();
member.setUsername(“memeber1”);
member.setHomeAddress(new Address(“homeCity”,”street”,”10000”));

member.getFavoriteFoods().add(“치킨”);
member.getFavoriteFoods().add(“족발”);
member.getFavoriteFoods().add(“피자”);

member.getAddressHistory().add(new Address(“old1”,”street”,”10000”));
member.getAddressHistory().add(new Address(“old2”,”street”,”10000”));

em.persist(member);

em.flush();
em.clear();

// 조회
System.out.println(“========START=======“);
Member findMember = em.find(Member.class, member.getId());

List<Address> addressHistory = findMember.getAddressHistory();
for(Address address : addressHistory) {
    System.out.println(“address.getCity() = “ + address.getCity());
}

em.flush();
em.clear();

// 수정
// 수정할때 임베디드 값 타입은 무조건 새로 생성해야 함 (new)
Member findMember = em.find(Member.class, member.getId());

Address a = findMember.getHomeAddress();
findMember.setHomeAddress(new Address(“newCity”, a.getStreet(), a.getZipcode()));

// 컬렉션 안의 치킨 -> 한식
findMember.getFavoriteFoods().remove(“치킨”);
findMember.getFavoriteFoods().add(“한식”);

```
-> 컬렉션은 기본적으로 지연로딩임
-> 임베디드 값 타입은 무조건 새로 생성해야 함
-> 컬렉션의 값만 변경해도 영속성 컨텍스트가 관리해줌 (em.persist 하지 않아도 됨)
(값 타입 컬렉션은 영속성 전에(Cascade) + 고아 객체 제 거 기능을 필수로 가진다고 볼 수 있다)


##### 값 타입 컬렉션 제약사항

- 값 타입은 엔티티와 다르게 식별자 개념이 없다
- 값은 변경하면 추적이 어렵다
- 값 타입 컬렉션에 변경 사항이 발생하면, 주인 엔티티와 연관된 모든 데이터를 삭제하고, 값 타입 컬렉션에 있는 현재 값을 모두 다시 저장한다
- 값 타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 기본 키를 구성해야 함: **null 입력 X, 중복 저장 X**


##### 값 타입 컬렉션 대안

- 실무에서는 상황에 따라 값 타입 컬렉션 대신에 일대다 관계를 고려
- 일대다 관계를 위한 엔티티를 만들고, 여기에서 값 타입을 사용
- 영속성 전이(Cascade) + 고아 객체 제거를 사용해서 값 타입 컬렉션 처럼 사용
- EX) AddressEntity
-> 체크 박스 같이 간단한 경우에 사용하기도 함

```java
// 실무에서는 일대다 관계로 구성함
// Member 에서 addressHistory
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = “MEMBER_ID”)
private List<AddressEntity> addressHistory = new ArrayList<>();

// AddressEntity
@Entity
@Table(name = “ADDRESS”)
public class AddressEntity {

    @Id
    @GeneratedValue
    private Long id;

    private Address address;
	  … // 생략
}
```


##### 정리

- 엔티티 타입 특징
	- 식별자 O
	- 생명주기 관리
	- 공유
- 값 타입 특징
	- 식별자 X
	- 생명주기를 엔티티에 의존
	- 공유하지 않는 것이 안전 (복사해서 사용)
	- 불변 객체로 만드는 것이 안전

-> 값 타입은 정말 값 타입이라 판단 될 때만 사용
-> 엔티티와 값 타입을 혼동해서 엔티티를 값 타입으로 만들면 안됨
-> 식별자가 필요하고, 지속해서 값을 추적, 변경해야 하면 그건 엔티티로 구성해야 함


---
## 실전 예제

[image:B03A5FE0-F792-415B-8C9C-2CA7DDBE5DC3-16850-00036A882BF58453/page40image50418768.png] 

```java
@Embeddable
public class Address {

    @Column(length = 10)
    private String city;
    @Column(length = 20)
    private String street;
    @Column(length = 5)
    private String zipcode;
    
    private String fullAddress() {
        return getCity() + “ “ + getStreet() + “ “ + getZipcode();
    }
	  … // 생략
}
```


