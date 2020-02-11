# #JPA/다양한연관관계매핑


## 연과관계 매핑시 고려해야 할 3가지

- 다중성
	- 다대일 : @ManyToOne
	- 일대다 : @OneToMany
	- 일대일 : @OneToOne
	- 다대다 : @ManyToMany (실무에서는 쓰면 안됨)

-> 헷갈릴때는 대칭성을 생각해야 한다 (반대로 생각해보면 됨)

- 단방향, 양방향
	- 테이블
		- 외래키 하나로 양쪽 조인 가능
		- 사실 방향이라는 개념이 없음
	- 객체
		- 참조용 필드가 있는 쪽으로만 참조 가능
		- 한쪽만 참조하면 단방향
		- 양쪽이 서로 참조하면 양방향 (사실 양방향은 없음, 단방향2개)

- 연관관계의 주인
	- 테이블은 외래키 하나로 두 테이블이 연관관계를 맺음
	- 객체 양방향 관계는 A -> B, B -> A 처럼 참조가 2군데
	- 객체 양방향 관계는 참조가 2군데 있음. 둘중 테이블의 외래키를 관리할 곳을 지정
	- 연관관계의 주인 : 외래키를 관리하는 참조
	- 주인의 반대편 : 외래키에는 영향을 주지 않음. 단순 조회만 가능


---
## 다대일 [N:1]

##### 다대일 단방향
[image:68A7A458-34D0-42E7-8AB6-4E18ACFCDB57-83586-00026C36D8DEA594/스크린샷 2020-02-11 오전 11.42.06.png]
-> DB 설계상 다대일에서 ‘다’ 쪽에 외래키가 가야함

- 가장 많이 사용하는 연관관계
- 다대일의 반대는 일대다

##### 다대일 양방향
[image:6472DAF3-D6E9-4C83-B475-969B6A103D74-83586-00026C9962C6C62C/스크린샷 2020-02-11 오전 11.48.59.png]
-> 단방향, 양방향에서 테이블은 바뀌지 않는다 (Team객체에 List<Member> 추가)

- 외래키가 있는 쪽인 연관관계의 주인
- 양쪽을 서로 참조하도록 개발

```java
// Member
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = “MEMBER_ID”)
    private Long id;

    @Column(name = “USERNAME”)
    private String username;

    @ManyToOne
    @JoinColumn(name = “TEAM_ID”)
    private Team team;
	  … // 생략
}

// Team
@Entity
public class Team {

    @Id @GeneratedValue
    @Column(name = “TEAM_ID”)
    private Long id;
    private String name;

 // 다대일 양방향
 // @OneToMany(mappedBy = “team”)
 // private List<Member> members = new ArrayList<>();
	  … // 생략
}
```


---
## 일대다 [1:N]

##### 일대다 단방향
[image:9FA3073B-C76F-4528-8EDC-4B76C943E2E5-83586-00026CE1B5F181B5/스크린샷 2020-02-11 오전 11.54.20.png]
-> 이 모델은 권장하지 않음 (실무에서 거의 안쓰임)
-> 객체 관계에서는 일대다 관계가 나올 수 있지만 DB상으로는 무조건 ‘다’에 외래키 있음

- 일대다 단방향은 일대다에서 일이 연관관계의 주인
- 테이블 일대다 관계는 항상 다쪽에 외래키가 있음
- 객체와 테이블의 차이 때문에 반대편의 외래키를 관리하는 특이한 구조
- @JoinColum을 꼭 사용해야 함. (안그럼 조인 테이블 방식을 사용, 중간에 테이블 하나 추가)
- 엔티티가 관리하는 외래키가 다른 테이블에 있음 (큰 단점)
- 연관관계 관리를 위해 추가로 UPDATE SQL 실행

-> 일대다 단방향 매핑보다는 다대일 양방향 매핑을 사용하자

##### 일대다 양방향
[image:6639AB71-AD8D-4B84-9156-B8A9E5F7B684-83586-00026E13D29DACA1/스크린샷 2020-02-11 오후 12.16.15.png]


```java
// Member
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = “MEMBER_ID”)
    private Long id;

    @Column(name = “USERNAME”)
    private String username;

	// insertable, updatable false로 읽기 전용으로 만들어서 양방향
	//@ManyToOne
	//@JoinColumn(name = “TEAM_ID”, insertable = false, updatable = false)
	//private Team team;

	  … // 생략
}

// Team
@Entity
public class Team {

    @Id @GeneratedValue
    @Column(name = “TEAM_ID”)
    private Long id;
    private String name;

    @OneToMany
    @JoinColumn(name = “TEAM_ID”)
    private List<Member> members = new ArrayList<>();
	  … // 생략
}

// JpaMain
Member member = new Member();
member.setUsername(“member1”);

em.persist(member);

Team team = new Team();
team.setName(“teamA”);

// Member의 TEAM_ID를 update 쿼리가 한번 더 나감
// 성능상 큰 차이는 없지만 잘 쓰지 않음
team.getMembers().add(member); 

em.persist(team);

tx.commit();
```
-> 코드를 보는 입장에서 Team 엔티티에 손을 대고, 비즈니스 로직을 짜는데 Member를 건드리는 쿼리에서 혼란이 많이 오게됨 (그래서 잘 안쓴다)

- 일대다 양방향이라는 매핑은 공식적으로 존재하지 않음
- @JoinColum(insertable = false, updatable = false)
- 읽기전용 필드를 사용해서 양방향처럼 사용

-> 다대일 양방향을 사용하자


---
## 일대일 [1:1]

- 일대일 관계는 반대도 일대일
- 주 테이블이나 대상 테이블 중에 외래키 선택 가능
	- 주 테이블 외래키
	- 대상 테이블의 외래키
- 외래키에 데이터베이스 유니크 제약조건 추가

##### 주 테이블에 외래키 단방향
[image:B85844B2-8D69-4030-9B11-778DBE3A6854-83586-00026F220B0F6939/스크린샷 2020-02-11 오후 12.47.20.png]

##### 주 테이블에 외래키 양방향
[image:CFD9ABDE-889F-4E76-8E56-0EF9800B0875-83586-00026F38E16317DC/스크린샷 2020-02-11 오후 12.48.58.png]
-> 다대일 양방향 매핑과 유사

```java
// Locker
@Entity
public class Locker {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToOne(mappedBy = “locker”)
    Member member;
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

    @OneToOne
    @JoinColumn(name = “LOCKER_ID”)
    private Locker locker;
	  … // 생략
}
```

##### 일대일 : 대상 테이블에 외래키 단방향
[image:0FA1C91E-AE03-436D-B78D-59A087CD80A0-83586-00026F844446E22A/스크린샷 2020-02-11 오후 12.54.22.png]
-> 일대일 : 대상 테이블에 외래키 단방향은 방법이 없음

- 단방향 관계는 JPA 지원 X
- 양방향 관계는 지원

##### 일대일 : 대상 테이블에 외래키 양방향
[image:92B3E9EA-3086-42FC-92A1-329A336D5C27-83586-00026FA594B66604/스크린샷 2020-02-11 오후 12.56.45.png]
-> 사실상 내가 내 엔티티를 관리함
-> 일대일 주 테이블에 외래키 양방향과 매핑 방법은 같음


##### 일대일 딜레마

DBA 입장 -> 차후에 멤버가 여러개의 라커를 가질 수 있는 경우 라커가 멤버아이디를 갖는 경우가 좋음 (반대로 한 라커를 여러 멤버가 공유한다면 반대임)

개발자 입장 -> 비즈니스 로직적으로 멤버에 라커가 있는게 성능적으로 유리함, 멤버에 접근했을때 라커를 가지고 있는지 여부 같은것을 한번에 알 수 있음 (조인 필요없음)


##### 정리
- 주 테이블에 외래키
	- 주 객체가 대상의 참조를 가지는 것 처럼 주 테이블 외래키를 두고 대상 테이블 찾음
	- 객체지향 개발자가 선호함
	- JPA 매핑 편리
	- 장점 : 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 확인 가능
	- 단점 : 값이 없으면 외래키에 null 허용해야 함

- 대상 테이블에 외래키
	- 대상 테이블에 외래키가 존재
	- 전통적인 데이터베이스 개발자 선호
	- 장점 : 주 테이블과 대상 테이블을 1:1에서 1:N 관계로 변경할 때 테이블 구조 유지
	- 단점 : 프록시 기능의 한계로 지연 로딩으로 설정해도 항상 즉시 로딩됨


---
## 다대다 [N:M]

실무에서 쓰면 안됨

- 중간테이블(연결테이블)을 엔티티로 승격시킴 -> 다대다를 다대일 + 일대다로 풀어냄
- 웬만하면 PK는 의미없는 값이 좋음 (GenerateValue 사용)

[image:5A11DC4E-5C96-41D8-8AF4-C40CDD76A6B7-83586-0002717B29D6BB3F/스크린샷 2020-02-11 오후 1.30.23.png]





