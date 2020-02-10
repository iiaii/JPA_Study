# #JPA/연관관계매핑기초


## 단반향 연관관계

### 객체를 테이블에 맞추어 모델링

[image:542BEC9A-189B-4BD1-A2CA-3FD70FE7752F-83586-000255597C52C19A/스크린샷 2020-02-10 오후 1.52.16.png]
-> 객체를 테이블에 맞추어 데이터 중심으로 연관관계를 맺으면 데이터를 넣거나 뺄때 뭔가 이상해짐 (객체 지향스럽지 않음)

```java
//팀 저장 Team team = new Team();  
team.setName(“TeamA”);  em.persist(team);  
//회원 저장 Member member = new Member();  
member.setName("member1");  member.setTeamId(team.getId());  em.persist(member);  
//조회  Member findMember = em.find(Member.class, member.getId()); 
//연관관계가 없음  Team findTeam = em.find(Team.class, team.getId()); 
```

즉, 객체를 테이블에 맞추어 데이터 중심으로 모델링하면, 협력관계를 만들 수 없다.


### 객체지향 모델링

[image:8CCE815E-20E1-4A51-9B30-3C565A0AB499-83586-0002558589D0635A/스크린샷 2020-02-10 오후 1.55.26.png]

[image:6F3026F3-5453-4C28-86FB-F38F4DFD8E83-83586-0002559009ABE6B8/스크린샷 2020-02-10 오후 1.56.08.png]
-> Member (N) : Team (1)

```java
// Member.java
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = “MEMBER_ID”)
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;
	  … // 생략
}

// JpaMain.java
// 저장, 조회 등
Team team = new Team();
team.setName(“TeamA”);
em.persist(team);

Member member = new Member();
member.setUsername("member1");
member.setTeam(team);
em.persist(member);

em.flush();
em.clear();

Member findMember = em.find(Member.class, member.getId());

Team findTeam = findMember.getTeam();
System.out.println("findTeam = " + findTeam.getName());

// findTeam = TeamA
```


---

## 양방향 연관관계와 연관관계의 주인

JPA에서 어려운 2가지
1. __영속성컨텍스트__
2. __연관관계주인__


### 양방향 매핑

[image:8E75F0BC-9E78-4404-9E1C-36645C8A3626-83586-00025621D2F40F7B/스크린샷 2020-02-10 오후 2.06.34.png]
-> 단방향 매핑에서 Team 객체에 List members만 추가된 형태

```java
// Team.java
@Entity
public class Team {

    @Id @GeneratedValue
    @Column(name = “TEAM_ID”)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
	  … // 생략
}

// JpaMain.java

Member findMember = em.find(Member.class, member.getId());
List<Member> members = findMember.getTeam().getMembers();

for(Member m : members) {
    System.out.println("m.getUsername() = " + m.getUsername());
}
```
-> 반대 방향으로도 객체 그래프 탐색이 가능


### 연관관계의 주인과 mappedBy

객체와 테이블간에 연관관계를 맺는 차이를 이해해야함
[image:3DDA6308-77A4-4706-86E0-476975A3EE9B-83586-000257CF4402EA9F/스크린샷 2020-02-10 오후 2.37.21.png]

- 객체 연관관계= 2개
	- 회원 -> 팀 연관관계 1개(단방향) 
	- 팀 -> 회원 연관관계 1개(단방향) 
	- 객체의 양방향관계는 서로다른 단방향관계 2개임 (객체를 양방향으로 참조하려면 단방향 연관관계를 2개 만들어야 함)
[image:CC6425FA-8871-469E-A39F-D43509179142-83586-000257FF10094191/스크린샷 2020-02-10 오후 2.40.44.png]

* 테이블 연관관계= 1개
	* 회원 <-> 팀의 연관관계 1개(양방향) 
	* 테이블은 **외래 키 하나**로 두 테이블의 연관관계를 관리 
	* MEMBER.TEAM_ID 외래 키 하나로 양방향 연관관계 가짐 (양쪽으로 조인할 수 있다.) 
[image:8AA4248F-87CF-4945-98F9-7E90D07D542A-83586-000257FC6CF15935/스크린샷 2020-02-10 오후 2.40.36.png]

그래서 둘중 하나로 외래 키를 관리해야 하는데
[image:2F48C3F0-B4ED-446A-98BA-1BA581B76EFF-83586-000258032C4CB536/스크린샷 2020-02-10 오후 2.41.05.png]

#### 양방향 매핑 규칙은 다음과 같다
- 객체의 두 관계중 하나를 연관관계의 주인으로 지정 
- **연관관계의 주인만이 외래 키를 관리****(****등록****,****수정****)**
- **주인이 아닌쪽은 읽기만 가능**
* 주인은 mappedBy 속성 사용X 
* 주인이 아니면 mappedBy 속성으로 주인 지정 

#### 누구를 주인으로?
- 1 대 N 관계에서 N쪽을 주인으로 정하는 것이 좋음
- 외래키가 있는 곳을 주인으로 정해라 (mappedBy 없는 쪽이 주인)
- 여기서는 Member.team이 연관관계의 주인

[image:B83DABB8-F40D-4D48-97A0-51217FA35708-83586-00025829250A9B3B/스크린샷 2020-02-10 오후 2.43.48.png]


### 주의점

```java
// 잘못된 경우!!
// 멤버가 연관관계의 주인인데 member.setTeam 말고 getMembers에서 add함
Member member = new Member();
member.setUsername(“member1”);
em.persist(member);

Team team = new Team();
team.setName(“TeamA”);
// 역방향(주인이 아닌 방향)만 연관관계 설정
team.getMembers().add(member); 
em.persist(team);

// 결과
// 멤버 테이블의 TEAM_ID가 null이 저장됨


// 제대로 된 경우
Team team = new Team();
team.setName(“TeamA”);
em.persist(team);

Member member = new Member();
member.setUsername("member1");
member.setTeam(team);
em.persist(member);

// 순수한 객체 관계를 고려해서 항상 양방향 매핑을 하는 것이 맞음
team.getMembers().add(member); 
// 1차 캐시에만 넣고 바로 조회하는 코드가 있다면 
// 양방향 매핑이 없는 코드는 에러가 발생할 수 있다
``` 

#### 양방향 연관관계 실습
- **순수 객체 상태를 고려해서 항상 양쪽에 값을 설정하자**
- 연관관계 편의 메서드를 생성하자
```java
// setTeam 보다 changeTeam으로 하면 관례가 아닌 어떤로직이 추가된걸 알림
// Member.java 
public void setTeam(Team team) { 
    this.team = team;
    team.getMembers().add(this); 
		// 원래는 새로 바뀌면 리스트에서 제거하고 넣어야함 (그 코드는 생략)
}

// 편의 메서드는 하나만!!
```
- 양방향 매핑시 무한루프를 조심하자
	- 예 : toString(), lombok, JSON 생성 라이브러리

-> toString, lombok은 사용하지말고, JSON 생성 라이브러리는 컨트롤러에 엔티티를 반환하면 안됨(DTO로 변환후 반환)
	
```java
// Member.java - toString()
@Override
public String toString() {
    return “Member{“ +
            “id=“ + id +
            “, username=‘” + username + ‘\’’ +
            ", team=" + team +
            '}';
}

// Team.java - toString()
@Override
public String toString() {
    return “Team{“ +
            “id=“ + id +
            “, name=‘” + name + ‘\’’ +
            ", members=" + members +
            '}';
}

// team의 toString을 호출하는데, team 객체의 리스트에서는 Member의 toString을 호출하기 때문에 무한루프에 빠짐 -> StackOverFlow
```


### 정리

- **단방향 매핑만으로 이미 연관관계 매핑은 완료** (설계 단계에서는) 
- 양방향 매핑은 반대 방향으로 조회(객체 그래프 탐색) 기능이 추가된 것 뿐
- JPQL 에서 역방향으로 탐색할 일이 많음
- 단방향 매핑을 잘하고 양방향은 필요할 때 추가해도 됨 (테이블에 영향을 주지 않음!)

연관관계의 주인을 정하는 기준

- 비즈니스 로직을 기준으로 연관관계의 주인을 선택하면 안됨
- **연관관계의 주인은 외래키의 위치를 기준으로 정해야 함**

단방향으로 잘 구성하는 것이 중요하다! (최대한 단방향으로 구성해라!)


