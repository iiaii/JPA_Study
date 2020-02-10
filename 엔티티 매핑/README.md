# #JPA/엔티티매핑


## 엔티티 매핑
- 객체와 테이블 매핑 : @Entity, @Table
- 필드와 컬럼 매핑 : @Column
- 기본 키 매핑 : @Id
- 연관관계 매핑 : @ManyToOne, @JoinColumn


---
## 객체와 테이블 매핑

### @Entity

클래스 위에 붙임

- @Enity 가 붙은 클래스는 JPA가 관리, 엔티티라 함
- JPA 를 사용해서 테이블과 매핑할 클래스는 @Entity 필수
- 주의
	- **기본 생성자 필수** (파라미터가 없는 public 또는 protected)
	- final 클래스, enum, interface, inner 클래스 사용 X
	- 저장할 필드에 final 사용 X

- 속성
	- name (@Entity(name = “Member”))
		- JPA에서 사용할 엔티티 이름을 지정
		- 기본값 : 클래스 이름을 그대로 사용 (예 : Member)
		- 같은 클래스 이름이 없으면 기본값 권장


### @Table

@Entity 밑에 지정

- @Table은 엔티티와 매핑할 테이블 지정

- 속성
	- name : 매핑할 테이블 이름 (기본값 - 엔티티 이름 사용)
	- catalog : 데이터베이스 catalog 매핑
	- schema : 데이터베이스 schema 매핑
	- uniqueConstraints (DDL) : DDL 생성 시에 유니크 제약 조건 생성

```java
@Entity
@Table(name = “MBR”, )
public class Member {
	// 생략
}
``` 


---
## 데이터 베이스 스키마 자동 생성

- DDL을 애플리케이션 실행 시점에 자동 생성
- 테이블 중심 -> 객체 중심
- 데이터베이스 방언을 활용해서 데이터베이스에 맞는 적절한 DDL 생성
- 이렇게 생성된 DDL은 **개발 장비에만 사용**
- 생성된 DDL은 운영서버에서 사용하지 않음 (다듬어서 사용하기는 함)


### 데이터베이스 스키마 자동생성 - 속성

[image:78100376-01BD-4170-9368-5C8ACC398C73-83586-00022649C0D6541B/스크린샷 2020-02-05 오후 7.58.45.png]

[image:82CE9147-D4DD-44B8-B3AE-8465B2786950-83586-0002265590B494EC/스크린샷 2020-02-05 오후 7.59.32.png]


### 주의점

- **운영장비에는 절대 create, create-drop, update 사용하면 안된다**
- 개발 초기 단계는 create 또는 update
- 테스트 서버는 update 또는 validate
- 스테이징과 운영서버는 validate 또는 none


### DDL 생성 기능

- 제약조건 추가 : 회원 이름은 필수, 10자 초과 X
	- @Column(nullable = false, length = 10)
- 유니크 제약 조건 추가
	- @Table(uniqueConstraints = {@UniqueConstraint(name = “NAME_AGE_UNIQUE”, columnNames = {“NAME”,”AGE”} )})
- DDL 생성 기능은 DDL을 자동 생성할 때만 사용되고 JPA의 실행 로직에는 영향을 주지 않는다 

```java
@Entity
public class Member {

    @Id
    private Long id;
    @Column(unique = true, length = 10)	
    private String name; // 10길이로 DDL 쿼리 나감, unique 업데이트

	…
}
```


---
## 필드와 컬럼 매핑

- @Column : 컬럼매핑

[image:B0FABF55-A090-48F7-A0EC-F01C29D7BF33-83586-00022865A1FE664B/스크린샷 2020-02-05 오후 8.37.23.png]

- @Temporal : 날짜 타입 매핑
	- LocalDate, LocalDateTime을 사용하면 생략가능
	- @Temporal(TemporalType.TIMESTAMP)

- @Enumerated : enum 타입 매핑
	- ORDINAL 사용하지 말것 (기본값이므로 바꿔야함)
	- 일반적으로 EnumType.STRING 사용 
	- @Enumerated(EnumType.STRING)

- @Lob : BLOB, CLOB(문자) 매핑
	- @Lob에는 지정할 수 있는 속성이 없음
	- CLOB : String, char[], java.sql.CLOB
	- BLOB : byte[], java.sql.BLOB

- @Transient : 특정 필드를 컬럼에 매핑하지 않음 (매핑 무시)
	- 필드 매핑 X
	- 데이터베이스에 저장, 조회 X
	- 주로 메모리상에서만 임시로 어떤 값을 보관하고 싶을 때 사용

```java
@Entity
public class Member {

    @Id
    private Long id;

    @Column(name = “name”)
    private String username;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

	  // java8 부터 사용되는 것은 어노테이션 없어도 됨
	  private LocalDate testDate;
	  private LocalDateTime testDateTime;

    @Lob
    private String description;
}
```


---
## 기본 키 매핑

### 기본키 매핑 어노테이션

- @Id
- @GeneratedValue

```java
@Id @GeneratedValue(strategy = GenerationType.AUTO) private Long id; 
```


### 기본 키 매핑 방법
- 직접 할당 : @Id만 사용
- 자동 생성 (@GeneratedValue)
	- **IDENTITY** : 데이터베이스에 위임, MYSQL
	- **SEQUENCE**: 데이터베이스 시퀀스 오브젝트 사용, ORACLE 
		- @SequenceGenerator 필요
	- **TABLE**: 키 생성용 테이블 사용, 모든 DB에서 사용 
		- @TableGenerator 필요
	- **AUTO**: 방언에 따라 자동 지정, 기본값 


### IDENTITY 전략

* 기본 키 생성을 데이터베이스에 위임 
* 주로 MySQL, PostgreSQL, SQL Server, DB2에서 사용
 (예: MySQL의 AUTO_ INCREMENT) 
* JPA는 보통 트랜잭션 커밋 시점에 INSERT SQL 실행 
* AUTO_ INCREMENT는 데이터베이스에 INSERT SQL을 실행 
한 이후에 ID 값을 알 수 있음 
* IDENTITY 전략은 em.persist() 시점에 즉시 INSERT SQL 실행 하고 DB에서 식별자를 조회 

```java
// 객체
@Entity 
￼
public class Member {
￼
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Long id;
	…


// 실행
// 비영속
            Member member = new Member();
//            member.setId(“ID_A”);
            member.setUsername(“C”);

            // 영속
            System.out.println(“==========“);
            em.persist(member);
     // 원래는 커밋시점이지만 ID 생성해주는 코드는 여기에서 insert 쿼리 나감
     // 영속성 컨텍스트에 pk로 1차캐시에 넣기 위함
	   // 내부적으로 바로 리턴을 받게 되있어서 알게됨
            System.out.println(“member.id = “ + member.getId());	// 출력 됨
            System.out.println(“==========“);

            tx.commit();
	// 보통은 커밋하는 시점에 쿼리가 나감
```


### SEQUENCE 전략 

- 데이터베이스 시퀀스는 유일한 값을 순서대로 생성하는 특별한 데이터베이스 오브젝트(예: 오라클 시퀀스) 
- 오라클, PostgreSQL, DB2, H2 데이터베이스에서 사용 

```java
@Entity 
@SequenceGenerator(
		name = “MEMBER_SEQ_GENERATOR”, 
		sequenceName = “MEMBER_SEQ”, //매핑할 데이터베이스 시퀀스 이름 
		initialValue = 1, allocationSize = 1)
public class Member {
	@Id 
	@GeneratedValue(strategy = GenerationType.SEQUENCE, 
	private Long id;
	generator = “MEMBER_SEQ_GENERATOR”) 
	…


```

[image:6ADCB7BA-EBB7-4194-8A2A-FDD46085886F-83586-0002461FC102771E/스크린샷 2020-02-06 오후 8.40.20.png]
-> allocationSize 를 사용해서 성능 최적화를 하는데 매번 insert 쿼리를 날리는 것이 아니라 allocationSize 만큼 확장하고 가져가는 방식

em.persist 하는 순간 시퀀스 전략인것을 알고 DB에서 값을 얻어온다. 그래서 insert 쿼리가 아니라 
[image:3DEAF1C5-8436-42D5-94B7-A1D38BFC1837-83586-000246601D402035/스크린샷 2020-02-06 오후 8.44.55.png]
이 쿼리를 날려서 PK 값을 가져온다. (allocationSize 값이 50이면 50을 한번에 가져옴)


### Table 전략

- 키 생성 전용 테이블을 하나 만들어서 데이터베이스 시퀀스를 흉 내내는 전략 
- 장점: 모든 데이터베이스에 적용 가능 
- 단점: 성능 
-> 실무에서 잘 안쓰임

*@Table*(name = “ORDERS”)

이와같이 클래스 위에 적으면 이름 변경 가능


