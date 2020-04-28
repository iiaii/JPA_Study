# #JPA/JPQL-중급

## 경로 표현식

[image:990F34F8-5C2B-49B1-B51E-B722F48CCF97-85545-00015CAB614C6FB3/스크린샷 2020-04-27 오후 8.16.08.png]

##### 용어 정리

- 상태 필드 (state field) : 단순히 값을 저장하기 위한 필드
- 연관 필드 (association field) : 연관관계를 위한 필드
	- 단일 값 연관 필드 :
	@ManyToOne, @OneToOne, 대상이 엔티티 (ex: m.team)
	- 컬렉션 값 연관 필드 :
	@OneToMany, @ManyToMany, 대상이 컬렉션 (ex: m.orders)


##### 특징

- 상태 필드 : 경로 탐색의 끝, 탐색 X
- 단일 값 연관 경로 : 묵시적 내부 조인 발생, 탐색 O
- 컬렉션 값 연관 경로 : 묵시적 내부 조인 발생, 탐색 X (가져오는 결과가 컬렉션인 경우)
	- FROM 절에서 명시적 조인을 통해 별칭을 얻으면 별칭을 통해 탐색 가능
	`select m.username From Team t join t.members m`


##### 상태 필드 경로 탐색

- JPQL : select m.username, m.age from Member m
- SQL : select m.username, m.age from Member m


##### 단일 값 연관 경로 탐색

- JPQL : select o.member from Order o
- SQL : select m.* from Orders o inner join Member m on o.member_id=m.id
(묵시적 조인을 피하도록 설계해야 함 ->  직관성 및 성능상의 문제 고려)


##### 명시적 조인, 묵시적 조인

- 명시적 조인 : join 키워드 직접 사용
	- select m from Member m join m.team t

- 묵시적 조인 : 경로 표현식에 의해 묵시적으로 SQL 조인 발생 (내부 조인만 가능)
	- select m.team from Member m


##### 예제

- select o.member.team from Order o -> 성공 (join이 2번 발생)
- select t.members from Team -> 성공 (join 1번 발생)
- select t.members.username from Team t -> 실패 (컬렉션 값 연관관계)
- select m.username from Team t join t.members m -> 성공 (명시적 조인)


##### 경로 탐색을 사용한 묵시적 조인 시 주의 사항

- 항상 내부 조인 발생
- 컬렉션은 경로 탐색의 끝, 명시적 조인을 통해 별칭을 얻어야 함
- 경로 탐색은 주로 SELECT, WHERE 절에서 사용하지만 묵시적 조인으로 인해 SQL의 FROM (JOIN) 절에 영향을 줌


##### 실무 조언

- **묵시적 조인 대신에 명시적 조인 사용 (묵시적 조인 쓰지 말 것!)**
- 조인은 SQL 튜닝에 중요한 포인트
- 묵시적 조인은 조인이 일어나는 상황을 한눈에 파악하지 어려움


---
## Fetch Join (실무에서 정말 중요!)

##### 페치 조인 (fetch join)

- SQL 조인 종류 X
- JPQL에서 **성능 최적화**를 위해 제공하는 기능
- 연관된 엔티티나 컬렉션을 **SQL 한 번에 조회**하는 기능
- join fetch 명령어 사용
- 페치 조인 :: = [ LEFT [OUTER] | INNER ] JOIN FETCH 조인 경로


##### 엔티티 페치 조인

- 회원을 조회하면서 연관된 팀도 함께 조회 (SQL 한번에)
- SQL을 보면 회원 뿐만 아니라 팀(T.*)도 함께 SELECT
- [JPQL] : `select m from Member m join fetch m.team`
- [SQL] : `select m.*,t.* from Member m inner join Team t on m.TEAM_ID=t.id`
(즉시 로딩과 비슷함)

[image:FE396C45-C1D6-446E-BD2D-19F98DB4B388-85545-0001899727B56EA4/스크린샷 2020-04-28 오후 10.23.48.png]

```java		  
String query1 = “select m from Member m”; // N + 1 문제 발생
String query2 = “select m from Member m join fetch m.team”;
List<Member> resultList = em.createQuery(query2, Member.class)
                    .getResultList();

for (Member member : resultList) {
	System.out.println(“member = “ + member.getUsername()+”, “+member.getTeam().getName());
// member1, teamA (SQL)
// member2, teamA (1차 캐시)
// member3, teamB (SQL)
// 회원 100명 -> N(지연로딩) + 1(회원조회) (최악의 경우 쿼리가 101번 나감)
}
```

[image:4E304314-749D-4443-B200-113A36267A9E-85545-0001891FCBC2CB52/스크린샷 2020-04-28 오후 10.15.20.png]


##### 컬렉션 페치 조인

- 일대다 관계, 컬렉션 페치 조인
- [JPQL]  
	`select t from Team t join fetch t.members where t.name=‘팀A’`
- [SQL]
	`select t.*, m.* from Team t inner join Member m on t.id=m.TEAM_ID where t.name=‘팀A’`
(일대다 조인은 데이터가 뻥튀기 될 수 있다)

[image:66DC44F3-2EAF-4520-A930-1162D9383570-85545-000189929D6A8A42/스크린샷 2020-04-28 오후 10.23.33.png]

[image:41A41C08-4964-4DFD-9790-5EFC858E38D1-85545-0001899DFC83252C/스크린샷 2020-04-28 오후 10.24.22.png]


##### 페치 조인과 DISTINCT

- SQL의 DISTINCT는 중복된 결과를 제거하는 명령
- JPQL의 DISTINCT 2가지 기능 제공
	- 1. SQL에 DISTINCT를 추가
	- 2. 애플리케이션에서 엔티티 중복 제거

- select distinct t from Team t join fetch t.members where t.name=‘팀A’
- SQL에 DISTINCT를 추가하지만 데이터가 다르므로 SQL 결과에서 중복제거 실패
- DISTINCT가 추가로 애플리케이션에서 중복 제거 시도
- 같은 식별자를 가진 Team엔티티 제거 

[image:EE3844FE-3802-4D0E-8915-A8A442314156-85545-00018A320E183143/스크린샷 2020-04-28 오후 10.34.57.png]


##### 페치 조인과 일반 조인의 차이

- 일반 조인 실행시 연관된 엔티티를 함께 조회하지 않음
- [JPQL]
	`select t from Team t join t.members m where t.name=‘팀A’`
- [SQL]
	`select t.* from Team t inner join Member m on t.id=m.TEAM_ID where t.NAME=‘팀A’`

- JPQL은 결과를 반환할 때 연관관계 고려 X
- 단지 SELECT 절에 지정한 엔티티만 조회할 뿐
- 여기서는 팀 엔티티만 조회하고, 회원 엔티티는 조회 X

- 페치 조인을 사용할 때만 연관된 엔티티도 함께 조회 (즉시 로딩)
- **페치 조인은 객체 그래프를 SQL 한번에 조회하는 개념**


---
## 페치 조인의 특징과 한계

- **페치 조인 대상에는 별칭을 줄 수 없다.** (객체 그래프 탐색을 모두 가능하게하기 위해 조작 불가능하도록 함-정합성 이슈)
	- 하이버네이트는 가능, 가급적 사용 X
- **둘 이상의 컬렉션은 페치 조인 할 수 없다.** (데이터 정합성 이슈)
- **컬렉션을 페치 조인하면 페이징 API(setFirstResult, setMaxResult)를 사용할 수 없다.**
	- 일대일, 다대일 같은 단일 값 연관 필드들은 페치 조인해도 페이징 가능
	- 하이버네이트는 경고 로그를 남기고 메모리에서 페이징 (매우 위험)
해결 -> 
1. 다대일로 뒤집어서 해결
2. @BatchSize(size = 100) (@OneToMany 위에 놓는다) -> 100개씩 가져옴
3. 글로벌 세팅(persistence.xml에서 “hibernate.default_batch_fetch_size” value=“100” 세팅)
4.  DTO로 뽑는다

- 연관된 엔티티들을 SQL 한 번으로 조회 - 성능 최적화
- 엔티티에 직접 적용하는 글로벌 로딩 전략보다 우선함
	- @OneToMany(fetch = FetchType.LAZY) // 글로벌 로딩 전략
- 실무에서 글로벌 로딩 전략은 모두 지연 로딩
- 최적화가 필요한 곳은 페치 조인 적용


##### 정리

- 모든 것을 페치 조인으로 해결할 수 는 없음
- 페치 조인은 객체 그래프를 유지할 때 사용하면 효과적
- 여러 테이블을 조인해서 엔티티가 가진 모양이 아닌 전혀 다른 결과를 내야하면, 페치 조인 보다는 일반 조인을 사용하고 필요한 데이터들만 조회해서 DTO로 반환하는 것이 효과적


---
## 다형성 쿼리

[image:FC31BCCE-49C7-4AE7-A452-8E478EED7932-85545-00018C4A30CC2B07/스크린샷 2020-04-28 오후 11.13.21.png]


##### TYPE

- 조회 대상을 특정 자식으로 한정
- 예) Item 중에 Book, Movie를 조회해라
- [JPQL] : `select i from Item i where type(i) IN (Book, Movie)`
- [SQL] : `select i from i where i.DTYPE in (‘B’,’M’)`


##### TREAT

- 자바의 타입 캐스팅과 유사
- 상속 구조에서 부모 타입을 특정 자식 타입으로 다룰 때 사용
- FROM, WHERE, SELECT 사용

- 예) 부모인 Item과 자식 Book이 있다.
- [JPQL] : `select i from Item i where treat(i as Book).auther=‘kim’`
- [SQL] : 
`select i.* from Item i where i.DTYPE=‘B’ and i.auther=‘kim’`


---
## 엔티티 직접 사용

##### 엔티티 직접 사용 - 기본 키 값

- JPQL에서 엔티티를 직접 사용하면 SQL에서 해당 엔티티의 기본 키 값을 사용
- [JPQL] : 
```
select count(m.id) from Member m // 엔티티의 아이디를 사용
select count(m) from Member m // 엔티티를 직젖 사용
```
- [SQL] :
`select count(m.id) as int from Member m`

[image:BF720F44-EEDC-480C-A9C7-68AA482A592D-85545-00018CA802DABFE5/스크린샷 2020-04-28 오후 11.20.04.png]

[image:D7371E36-B3C2-4680-AF13-29296C5A461B-85545-00018CAAD71B65BC/스크린샷 2020-04-28 오후 11.20.16.png]


---
## Named 쿼리

[image:C4C00F0C-6840-4367-9821-B0E98F9E40D7-85545-00018D068279E849/스크린샷 2020-04-28 오후 11.26.50.png]
[image:631ABBF8-B84E-4BCD-896C-412ACFE0AB62-85545-00018D1B81F0FA59/스크린샷 2020-04-28 오후 11.28.19.png]

##### Named 쿼리 - 정적 쿼리

- 미리 정의해서 이름을 부여해두고 사용하는 JPQL
- 정적 쿼리
- 어노테이션, XML에 정의
- 애플리케이션 로딩 시점에 초기화 후 재사용
- **애플리케이션 로딩 시점에 쿼리를 검증**
- XML이 항상 우선권을 가진다.
- 애플리케이션 운영 환경에 따라 다른 XML을 배포할 수 있다.
- Sprint data JPA 로 보다 쉽게 사용가능

```java
// Member.java
@Entity
@NamedQuery(
        name = “Member.findByUsername”,
        query = “select m from Member  m  where m.username = :username”
)
public class Member {
… // 생략
}

// JpaMain
List<Member> resultList = em.createNamedQuery(“Member.findByUsername”, Member.class)
        .setParameter(“username”, “member1”)
        .getResultList();

for (Member member : resultList) {
    System.out.println(“member = “ + member);
}
```


---
## 벌크 연산

##### 벌크 연산 (update, delete)

- 재고가 10개 미만인 모든 상품의 가격을 10% 상승하려면?
- JPA 변경 감지 기능으로 실행하려면 너무 많은 SQL 실행
	- 1. 재고가 10개 미만인 상품을 리스트로 조회한다.
	- 2. 상품 엔티티의 가격을 10% 증가한다.
	- 3. 트랜잭션 커밋 시점에 변경감지가 동작한다.
- 변경된 데이터가 100건이라면 100번의 UPDATE SQL 실행


##### 예제

- 쿼리 한번으로 여러 테이블 로우 변경 (엔티티)
- executeUpdate()의 결과는 영향받은 엔티티 수 반환
- UPDATE, DELETE 지원
- INSERT(insert into .. select, 하이버테이트 지원)

[image:3F2EA2EC-41C6-4796-82DD-D8413A26A327-85545-00018DC27054F7C9/스크린샷 2020-04-28 오후 11.40.17.png]


##### 주의

- 벌크 연산은 영속성 컨텍스트를 무시하고 데이터베이스에 직접 쿼리
	- 벌크 연산을 먼저 실행
	- **벌크 연산 수행 후 영속성 컨텍스트 초기화**
(flush는 commit을 하거나, 쿼리, 강제호출하면 발생)

