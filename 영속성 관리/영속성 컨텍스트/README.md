# #JPA/영속성컨텍스트

## JPA에서 가장 중요한 2가지
- 객체와 관계형 데이터베이스 매핑하기 (Object Relational Mapping)
	DB, 객체 어떻게 설계해서 어떻게 매핑해서 쓸 것인가 (정적임)
- 영속성 컨텍스트
	실제 JPA가 내부에서 어떻게 동작하는가


## 엔티티 매니저 팩토리와 엔티티 매니저
EntityManagerFactory ->(생성) EntityManager1
					 ->(생성) EntityManager2 ->(사용) 커넥션 


## 영속성 컨텍스트
- JPA를 이해하는데 가장 중요한 용어
- 엔티티를 영구 저장하는 환경 이라는 뜻
- EntityManager.persist(entity); 
->  (엔티티를 디비에 저장하는 것이 아니라) 엔티티를 영속성 컨텍스트에 저장한다는 뜻
- 논리적인 개념 (눈에 보이는 것이 아님)
- 엔티티 매니저를 통해서 영속성 컨텍스트에 접근함
(J2SE환경 : 엔티티 매니저 -> 영속성 컨텍스트 1:1 / 스프링 프레임워크 같은 컨테이너 환경 : 엔티티 매니저와 영속성 컨텍스트가 N:1)


## 엔티티 생명주기
- 비영속 (new/transient) : 영속성 컨텍스트와 전혀 관계 없는 **새로운** 상태

[image:FB7BD162-B719-4ED3-BD9D-DAEC1FF9B637-83586-000209BA05E20931/스크린샷 2020-02-04 오후 7.52.39.png]

```java
// 객체를 생성한 상태 (비영속)
Member member = new Member();
member.setId(“member1”);
member.setUsername(“회원1”);
```

- 영속 (managed) : 영속성 컨텍스트에 **관리**되는 상태

[image:E482E828-597E-4778-81D9-DCA7ED6ABD0A-83586-000209D54A2089E3/스크린샷 2020-02-04 오후 7.54.36.png]

```java
// 객체를 생성한 상태 (비영속)
Member member = new Member();
member.setId(“member1”);
member.setUsername(“회원1”);

EntityManager em = emf.createEntityManager();
em.getTransaction().begin();

// 객체를 저장한 상태 (영속)
// 이때 db에 저장되는것이 아님 (이때 쿼리가 날라가는 것이 아님)
em.persisit(member);
```

- 준영속 (detached) : 영속성 컨텍스트에 저장되었다가 **분리**된 상태

```java
// 회원 엔티티를 영속성 컨텍스트에서 분리, 준영속 상태
// 영속성 컨텍스트에서 지움
em.detach(member);
```


- 삭제 (removed) : **삭제**된 상태

```java
// 객체를 삭제한 상태(삭제)
// db 삭제를 요청
em.remove(member);
```


## 영속성 컨텍스트의 이점

컬렉션에 넣는 것처럼 사용하는 것이 포인트

- 1차 캐시

```java
// 객체를 생성한 상태 (비영속)
Member member = new Member();
member.setId(“member1”);
member.setUsername(“회원1”);

// 객체를 저장한 상태 (영속)
// 1차 캐시에 저장됨
em.persisit(member);

// 1차 캐시에서 조회
Member findMember = em.find(Member.class, “member1”);
// 1차 캐시에 조회(없음) -> DB 조회 -> 1차캐시 저장 -> 반환
Member findMember2 = em.find(Member.class, “member2”);
```

-> 사실 큰 도움은 안됨 (고객의 요청이 끝나면 영속성 컨텍스트는 날라감)
(전체에서 공유하는 것은 2차캐시라함)

[image:3283AE5D-41E8-4039-BF3A-8BDBC603E999-83586-00020ABB7206EB78/스크린샷 2020-02-04 오후 8.11.05.png]
[image:AEBEB068-512B-46B6-8B62-DBCF73919073-83586-00020ADAD9D98AB9/스크린샷 2020-02-04 오후 8.13.21.png]
[image:8DFA51EA-B066-49FA-A529-68F9820A98B0-83586-00020AE423F6F65D/스크린샷 2020-02-04 오후 8.13.58.png]

- 동일성(identity) 보장

```java
Member a = em.find(Member.class, “member1”);
Member b = em.find(Member.class, “member1”);
System.out.println(a == b); // true - 동일성 비교 
```

(같은 트랙잭션일 때 가능한 상황)
1차 캐시로 반복 읽기(REPEATABLE READ) 등급의 트랜잭션 격리 수준을 데이터베이스가 아닌 애플리케이션 차원에서 제공

- 트랜잭션을 지원하는 쓰기 지연 (transaction write-behind)

Member 객체에서
JPA의 경우 내부적으로 리플렉션 (동적으로 객체생성) 때문에 기본생성자가 있어야 함 

엔티티 등록
```java
EntityManaber em = emf.createEntityManager();
EntityTransaction transaction = em.getTransaction();
// 엔티티 매니저는 데이터 변경시 트랜잭션을 시작해야 한다
transaction.begin();

em.persist(memberA);
em.persist(memberB);
// 여기까지 INSERT SQL을 데이터베이스에 보내지 않는다

// 커밋하는 순간 데이터베이스에 INSERT SQL을 보낸다
transaction.commit();	// [ 트랜잭션 ] 커밋
```

[image:5C54B1D7-5381-4118-9239-37E65F873B90-83586-00020BC90F9F96D1/스크린샷 2020-02-04 오후 8.30.24.png]
[image:C7E7DD78-E7E4-483A-9F67-248E1958D57C-83586-00020BCBE9ABE0B8/스크린샷 2020-02-04 오후 8.30.37.png]

- 변경 감지 (Dirty Checking)

```java
EntityManaber em = emf.createEntityManager();
EntityTransaction transaction = em.getTransaction();
transaction.begin(); // 트랜잭션 시작

// 영속 엔티티 조회
Member memberA = em.find(Member.class, “memberA”);

// 영속 엔티티 수정
memberA.setUsername(“hi”);
memberA.setAge(10);

// em.update(member) 같은 코드가 있어야 될것 같지만 없어도 변경됨
transaction.commit(); // 트랜잭션 커밋
```

[image:6B073155-1A4B-441B-9AC6-833CCA57D524-83586-00020CA1CADE0DD4/스크린샷 2020-02-04 오후 8.45.52.png]

```java
// 삭제 대상 엔티티 조회
Member meberA = em.find(Member.class, “memberA”);
em.remove(memberA); // 엔티티 삭제
```

- 지연 로딩 (Lazy Loading)




