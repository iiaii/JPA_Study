# #JPA/프록시와연관관계관리

## 프록시

Member 를 조회할때 Team도 함께 조회해야 되는가?

`Member findMember = em.find(Member.class, 1L);`
-> 비즈니스 로직상 팀이 필요한 경우가 거의 없는데 매번 팀까지 같이 가져온다..

- em.find() vs em.**getReference()**
* em.find(): 데이터베이스를 통해서 실제 엔티티 객체 조회 
* em.getReference(): **데이터베이스 조회를 미루는 가짜****(****프록시****)****엔티티 객체 조회**

* 실제 클래스를 상속 받아서 만들어짐 
* 실제 클래스와 겉 모양이 같다. 
* 사용하는 입장에서는 진짜 객체인지 프록시 객체인지 구분하지 않고 사용하면 됨(이론상) 

[image:6D2F2B0E-7EA9-48CA-8188-05F6421748D5-83586-0002BEA459D8A851/page7image27537664.png]

- 프록시 객체는 실제 객체의 참조(target)를 보관
* 프록시 객체를 호출하면 프록시 객체는 실제 객체의 메소드 호출 

[image:20348442-758A-4BAA-B056-C425578AAD0D-83586-0002BECEED10B0CA/page8image27468208.png] 

```java
Member member = em.getReference(Member.class, “id1”); member.getName(); 
```

[image:109C9B6A-4C54-42A1-8225-56EEDE10F5A8-83586-0002BED703BF998F/스크린샷 2020-02-14 오후 5.56.54.png]

- 프록시 객체는 처음 사용할때 한번만 초기화 
- 프록시 객체를 초기화 할 때, 프록시 객체가 실제 엔티티로 바뀌는 것은 아님, 초기화되면 프록시 객체를 통해서 실제 엔티티에 접근 가능 
* 프록시 객체는 원본 엔티티를 상속받음, 따라서 타입 체크시 주의해야함 (== 비 교 실패, 대신 instance of 사용) 
* 영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.**getReference()**를 호출해 도 실제 엔티티 반환 
* 영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태일 때, 프록시를 초기화하면 문제 발생 (하이버네이트는 org.hibernate.LazyInitializationException 예외를 터트림 / 실무에서 많이 만나는 문제) 


```java
// 저장
Member member = new Member();
member.setUsername(“hello”);

em.persist(member);

em.flush();
em.clear(); // 영속성 컨텍스트 비움

Member findMember = em.getReference(Member.class, member.getId());
System.out.println(“findMember = “ + findMember.getClass());    // proxy 클래스가 출력됨
System.out.println(“findMember.getId() = “ + findMember.getId()); // id는 insert와 동시에 알수 있음, 바로 출력
System.out.println(“findMember.getUsername() = “ + findMember.getUsername()); // 이 시점에 쿼리날림 (영속성 컨텍스트에 요청)

em.flush();
em.clear(); // 영속성 컨텍스트 비움

// 조회순서 (find -> getReference)
// em.find로 영속성 컨텍스트에 올라가면 그 타입을 유지한다.
// 컬렉션에서 데이터를 저장하고 빼는것 처럼 같은 타입을 유지하려고 하기 때문
Member m1 = em.find(Member.class, member.getId());
System.out.println(m1.getClass()); // Member

Member m2 = em.getReference(Member.class, member.getId());
System.out.println(m2.getClass()); // Member

// 이와 반대의 순서로 하면 (getReference -> find)
Member m3 = em.getReference(Member.class, member.getId());
System.out.println(m3.getClass()); // 프록시 Member

Member m4 = em.find(Member.class, member.getId());
System.out.println(m4.getClass()); // 프록시 Member

tx.commit();
```
-> 컬렉션에 넣어서 사용하는 것처럼 하기 위해서 꺼낸 객체의 타입이 항상 동일하게 끔 유지한다. (equals 에 혼란이 없도록, 상황에 따라 proxy 타입 객체, 실제 타입 객체로 유지됨)


##### 프록시 확인

- **프록시 인스턴스의 초기화 여부 확인**
 PersistenceUnitUtil.isLoaded(Object entity) 

* **프록시 클래스 확인 방법**
 entity.getClass().getName() 출력(..javasist.. or HibernateProxy…) 

- **프록시 강제 초기화**
 org.hibernate.Hibernate.initialize(entity); 

* 참고: JPA 표준은 강제 초기화 없음
 강제 호출: **member.getName()**



---
## 즉시로딩과 지연로딩

##### 지연로딩 (LAZY)

[image:239D0980-51CF-4B2D-98B1-173F6A112B4A-83586-0002DD415F2872A6/page15image27494144.png] 

```java
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = “MEMBER_ID”)
    private Long id;

    @Column(name = “USERNAME”)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Team team;
	  … // 생략
}

//
Team team = new Team();
team.setName(“teamA”);
em.persist(team);

Member member1 = new Member();
member1.setUsername(“member1”);
member1.setTeam(team);
em.persist(member1);

em.flush();
em.clear();

Member m = em.find(Member.class, member1.getId());
System.out.println(“m = “ + m.getTeam().getClass()); // 프록시 

System.out.println(“============“);
m.getTeam().getName();	// 이때 쿼리 발생
System.out.println(“============“);
```

```java
Member member = em.find(Member.class, 1L);

Team team = member.getTeam();
team.getName(); // 실제 team을 사용하는 시점에 초기화 (DB 조회)
```


##### 즉시로딩 (EAGER)

[image:113D6F42-0331-47A5-A59D-B072F462B355-83586-0002DD99EB65B24A/page19image27375504.png] 

JPA 구현체는 가능하면 조인을 사용해서 SQL 한번에 조회
(@ManyToOne, @OneToOne 은 아무것도 설정하지 않으면 즉시로딩함)


##### 주의점

- 가급적 지연 로딩만 사용(특히 실무에서)
- 즉시 로딩을 적용하면 예상하지 못한 SQL이 발생 
- 즉시 로딩은 JPQL에서 N+1 문제를 일으킨다
	- 해결방안 -> 모든 연관관계를 Lazy 로 설정하고
	- Fetch Join 함 (런타임에 동적으로 원하는 것을 선택해서 가져옴)

- @ManyToOne, @OneToOne은 기본이 즉시 로딩 -> LAZY 로 설정
- @OneToMany, @ManyToMany는 기본이 지연 로딩 


##### 지연로딩 활용

-> 실무에서는 다 지연로딩으로 해야함

- 모든 연관관계에 지연 로딩을 사용해라!
- 실무에서 즉시 로딩을 사용하지 마라!
- JPQL fetch 조인이나, 엔티티 그래프 기능을 사용해라!
- 즉시 로딩은 상상하지 못한 쿼리가 나간다


---
## 영속성 전이(CASCADE)와 고아 객체


### 영속성 전이 (CASCADE)

`@OneToMany(mappedBy=“parent”, cascade=CascadeType.PERSIST)`

[image:4C0F3491-C7D8-4E0D-856B-2FDDEA21D2AA-83586-0002DF716CDA400A/스크린샷 2020-02-18 오후 4.03.40.png]


##### 주의점

- 영속성 전이는 연관관계를 매핑하는 것과 아무 관련이 없음 
- 엔티티를 영속화할 때 연관된 엔티티도 함께 영속화하는 편리함을 제공할 뿐 


##### 종류

- ALL : 모두 적용 (많이 쓰임)
- PERSIST : 영속 (많이 쓰임)
- REMOVE : 삭제
- MERGE : 병합 
- REFRESH : REFRESH 
- DETACH : DETACH 

```java
Child child1 = new Child();
Child child2 = new Child();

Parent parent = new Parent();
parent.addChild(child1);
parent.addChild(child2);

em.persist(child1);
em.persist(child2); 
em.persist(parent);
// 기존에는 이렇게 적어야 함


// cascade all 설정하면
@Entity
public class Parent {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = “parent”, cascade = CascadeType.ALL)
    private List<Child> childList = new ArrayList<>();

    public void addChild(Child child) {
        childList.add(child);
        child.setParent(this);
    }
	  … // 생략
}

// JpaMain
Child child1 = new Child();
Child child2 = new Child();

Parent parent = new Parent();
parent.addChild(child1);
parent.addChild(child2);

em.persist(parent); // 한번만 호출해도 child 관리됨
```
-> 파일을 여러 군데에서 관리하면 사용 X, 한 곳에서만 사용하는 경우에는 사용 O (단일 사용자일 경우만 사용 권장)


### 고아 객체

고아 객체 제거: 부모 엔티티와 연관관계가 끊어진 자식 엔티티 를 자동으로 삭제 

```java
@Entity
public class Parent {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = “parent”, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Child> childList = new ArrayList<>();
	  … // 생략
}

// JpaMain
Child child1 = new Child();
Child child2 = new Child();

Parent parent = new Parent();
parent.addChild(child1);
parent.addChild(child2);

em.persist(parent);

em.flush();
em.clear();

Parent findParent = em.find(Parent.class, parent.getId());
findParent.getChildList().remove(0); // delete 쿼리 발생
```


##### 주의점

- 참조가 제거된 엔티티는 다른 곳에서 참조하지 않는 고아 객체로 보고 삭제하는 기능 
- **참조하는 곳이 하나일 때 사용해야함****!**
- **특정 엔티티가 개인 소유할 때 사용** (중요!)
- @OneToOne, @OneToMany만 가능 
- 참고: 개념적으로 부모를 제거하면 자식은 고아가 된다. 따라서 고 아 객체 제거 기능을 활성화 하면, 부모를 제거할 때 자식도 함께 제거된다. 이것은 CascadeType.REMOVE처럼 동작한다. 


##### 영속성 전이 + 고아 객체 , 생명주기

**CascadeType.ALL + orphanRemovel=true**
- 스스로 생명주기를 관리하는 엔티티는 em.persist()로 영속화, em.remove()로 제거 
- 두 옵션을 모두 활성화 하면 부모 엔티티를 통해서 자식의 생명 주기를 관리할 수 있음 
- 도메인 주도 설계(DDD)의 Aggregate Root개념을 구현할 때 유용 





