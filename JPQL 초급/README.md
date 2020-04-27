# #JPA/JPQL-초급

## JPQL 소개

###### 실무에서 대부분 JPQL 로 해결이 되지만 DB 종속적인 케이스에서는 네이티브 SQL 이나 mybatis를 사용


##### JPQL

 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL이 필요 -> 예를 들어 나이가 18세 이상인 회원 모두 검색

- JPA는 SQL을 추상화한 JPQL이라는 객체지향 쿼리 언어 제공
- SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원
- JPQL은 엔티티 객체를 대상으로 쿼리 
- SQL은 데이터베이스 테이블을 대상으로 쿼리 

```java
// JPQL 예시
List<Member> resultList = em.createQuery(
        “select m From Member m where m.username like ‘%kime%’”,
        Member.class
).getResultList();
```


- 테이블이 아닌 객체를 대상으로 검색하는 객체 지향 쿼리 
- SQL을 추상화해서 특정 데이터베이스 SQL에 의존X 
-> JPQL을 한마디로 정의하면 객체 지향 SQL 


##### criteria

```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Member> query = cb.createQuery(Member.class);

Root<Member> m = query.from(Member.class);

CriteriaQuery<Member> cq = query.select(m).where(cb.equal(m.get(“username”), “kim”));
List<Member> resultList = em.createQuery(cq).getResultList();
```

문자가 아닌 자바 코드로 JPQL을 작성할 수 있어서 동적 쿼리를 작성하기 쉬움 (JPQL 빌더로서 컴파일 에러 방지)
하지만 알아보기 힘들어서 **실무에서는 안쓰임** (유지보수가 힘들어짐)
-> QueryDSL 사용을 권장


##### QueryDSL (실무 사용권장)

```java
//JPQL //select m from Member m where m.age > 18 
JPAFactoryQuery query = new JPAQueryFactory(em);  QMember m = QMember.member; 
List<Member> list = 
query.selectFrom(m)
	.where(m.age.gt(18)) 
	.orderBy(m.name.desc())
	.fetch(); 
```
-> JPQL 만 잘하면 QueryDSL은 그냥 해결


##### 네이티브 SQL

```java
em.createNativeQuery(“select MEMBER_ID, city, street, zip code, USERNAME from MEMBER”).getResultList();
```
-> 실무에서는 잘 안쓰고 SpringJdbcTemplate을 보통 사용하지만 db 커넥션과 영속성 컨텍스트가 따로 관리되므로 영속성 컨텍스트를 적절한 시점에 강제로 플러시 필요
(기본적으로 flush 는 tx.commit, em.query 할 때 발생)

[image:3D3F56C2-2BB5-4282-8171-7370CDEEAF65-85545-000151EC9866E29E/스크린샷 2020-04-26 오후 10.54.46.png]


---
## JPQL (Java Persistence Query Language)

- JPQL은 객체지향 쿼리 언어다.따라서 테이블을 대상으로 쿼리 하는 것이 아니라 **엔티티 객체를 대상으로 쿼리**한다
- JPQL은 SQL을 추상화해서 특정데이터베이스 SQL에 의존하지 않는다
- JPQL은 결국 SQL로 변환된다


##### 문법

[image:DCFBD110-EC92-41D7-A4A0-8B027D0F94BC-85545-00015201925935CF/스크린샷 2020-04-26 오후 10.56.19.png]

```
select m from **Member**as m where **m.age**> 18

select
	COUNT(m), //회원수 
	SUM(m.age), //나이 합 
	AVG(m.age), //평균 나이 
	MAX(m.age), //최대 나이
	MIN(m.age) //최소 나이 
from Member m 

// 집합과 정렬
// GROUP BY, HAVING
// ORDER BY
```

* 엔티티와 속성은 대소문자 구분O (Member, age) 
* JPQL 키워드는 대소문자 구분X (SELECT, FROM, where) 
* 엔티티 이름 사용, 테이블 이름이 아님(Member) 
* **별칭은 필수 (m)**(as는 생략가능) 


##### TypeQuery, Query

```java
// TypedQuery : 반환 타입이 명확할 때 사용
TypedQuery<Member> query1 = em.createQuery(“select m from Member m”, Member.class);
TypedQuery<String> query2 = em.createQuery(“select m.username from Member m”, String.class);

// Query : 반환 타입이 명확하지 않을 때 사용
Query query3 = em.createQuery(“select m.username, m.age from Member m”);
```


##### 결과 조회 API

```java
// 1. 가져온 값이 여러개일 때
// 결과가 없으면 빈 리스트 반환 
TypedQuery<Member> query1 = em.createQuery(“select m from Member m”, Member.class);
List<Member> members = query1.getResultList();

for(Member m : members) {
	Systme.out.println(m.username);
}


// 2. 가져온 값이 한개일 경우
// 결과가 없으면 javax.persistance.NoResultException
// 결과가 둘 이상이면 javax.persistance.NonUniqueResultException
// >> 결과가 하나일 때만 사용해야 한다.
Member member1 = query1.getSingleResult();
```
-> 요즘 Spring Data JPA 결과 없으면 Optional로 반환 


##### 파라미터 바인딩 - 이름 기준 (위치 기준은 사용하지 말것)

```java
// 이름 기준
TypedQuery<Member> query = em.createQuery(“select m from Member m where m.username = :username”, Member.class);
query.setParameter(“username”, “member1”);
Member member1 = query.getSingleResult(); 

// 메서드 체이닝 (권장)
Member member1 = m.createQuery(“select m from Member m where m.username = :username”, Member.class)
	.setParameter(“username”, “member1”)
	.getSingleResult();
```


---
## 프로젝션 (SELECT)

##### 일반적인 경우

- SELECT 절에 조회할 대상을 지정하는 것
- 프로젝션 대상: 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자등 기본 데이터 타
입)
- SELECT m FROM Member m -> 엔티티 프로젝션 
- SELECT m.team FROM Member m -> 엔티티 프로젝션
• SELECT m.address FROM Member m -> 임베디드 타입 프로젝션
- SELECT m.username, m.age FROM Member m -> 스칼라 타입 프로젝션
- DISTINCT로 중복 제거


##### 여러 값 조회하는 경우

1. Query 타입으로 조회

```java
List lists = em.createQuery(“select m.username, m.age from Member m”);
Object o = lists.get(0);
Object[] result = (Object[]) o;
System.out.println(“username = ”+result[0]);
System.out.println(“age = ”+result[1]);
```

2. Object[] 타입으로 조회 (TypeQuery)


```java
List<Object[]> list = em.createQuery(“select m.username, m.age from Member m”);

Object[] result = list.get(0);
System.out.println(“username = ”+result[0]);
System.out.println(“age = ”+result[1]);
```

3. new 명령어로 조회

- 단순 값을 DTO (Data Transfer Object)로 바로 조회
- 패키지 명을 포함한 전체 클래스 명 입력
- 순서와 타입이 일치하는 생성자 필요

```java
List<MemberDTO> list = em.createQuery(“select new jpql.MemberDTO(m.username, m.age) from Member m”, MemberDTO.class);

MemberDTO memberDTO = list.get(0);
System.out.println(“username = ”+memberDTO.getUsername());
System.out.println(“age = ”+memberDTO.getAge());

class MemberDTO {
	private String username;
	private int age;

	// 생성자, getter 생략
}
```


---
## 페이징 API

- JPA는 페이징을 2가지 API로 추상화
	- SetFirstResult(int startPosition) : 조회 시작 위치 (0부터 시작)
	- SetMaxResults(int maxResult) : 조회할 데이터 수

```java
// 예제 (try-catch로 감싸진 JpaMain 코드)
for (int i = 0; i < 100 ; i++) {
    Member member = new Member();
    member.setUsername(“member”+i);
    member.setAge(i);
    em.persist(member);
}

em.flush();
em.clear();

List<Member> resultList = em.createQuery(“select m from Member m order by m.age desc”, Member.class)
        .setFirstResult(0)  // 시작
        .setMaxResults(10)  // 끝
        .getResultList();

System.out.println("resultList.size() = " + resultList.size());
for (Member member1 : resultList) {
    System.out.println(“member1 = “ + member1);
}

tx.commit();

// 결과 : 99 -> 90 까지 출력
```


---
## 조인

##### JOIN 종류

- 내부 조인 : SELECT m FROM Member m [INNER] JOIN m.team t
- 외부 조인 : SELECT m FROM Member m LEFT [OUTER] JOIN m.team t
- 세타 조인 : SELECT count(m) from Member m, Team t where m.username = t.name
([]괄호는 생략 가능)


##### ON 절

- ON 절을 활용한 조인 
	- 조인 대상 필터링
	- 연관관계 없는 엔티티 외부 조인

1.  조인 대상 필터링

- 예) 회원과 팀을 조인하면서 팀이

**JPQL** : 
`select m, t from Member m left join m.team t on t.name = ‘A’`
**SQL** :
`select m.*, t.* from Member m left join Team t on m.TEAM_ID=t.id and t.name=‘A’`

2. 연관관계 없는 엔티티 외부 조인

- 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인

**JPQL** :
`select m, t from Member m left join Team t on m.username=t.name`
**SQL** :
`select m.*, t.* from Member m left join Team t on m.username=t.name’`


---
## 서브 쿼리

##### 서브 쿼리

- 나이가 평균보다 많은 회원

```
select m from Member m 
where m.age > (select avg(m2.age) from Member m2)
```

- 한 건이라도 주문한 고객

```
select m from Member m
where (select count(o) from Order o where m = o.memeber) > 0
```


##### 서브쿼리 지원 함수

- [NOT] EXISTS (subquery) : 서브쿼리에 결과가 존재하면 참
	- {ALL | ANY | SOME} (subquery)
	- ALL 모두 만족하면 참
	- ANY, SOME : 같은 의미 조건을 하나라도 만족하면 참
- [NOT] IN (subquery) : 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참


##### 예제

- 팀 A 소속인 회원

```
select m from Member m
where exists (select t from m.team t where t.name = ‘팀A’)
```

- 전체 상품 각각의 재고보다 주문량이 많은 주문들

```
select o from Order o
where o.orderAmount > ALL(select p.stockAmount from Product p)
```

- 어떤 팀이든 팀에 소속된 회원

```
select m from Member m
where m.team = ANY (select t from Team t)
```


##### JPA 서브 쿼리 한계

- JPA는 WHERE, HAVING 절에서만 서브 쿼리 사용 가능
- SELECT 절도 가능 
- **FROM 절의 서브 쿼리는 현재 JPQL에서 불가능** (인라인 뷰)
	- **조인으로 풀어서 해결**

```
// 이런게 불가능함
select mm.age, mm.username 
from (select m.age, m.username from Member m) as mm
```


---
## JPQL 타입 표현과 기타식

##### JPQL 타입 표현

- 문자 : ‘HELLO’, ’She’’s’
- 숫자 : 10L (Long), 10D (Double), 10F (Float)
- Boolean : TRUE, FALSE
- ENUM : jpabook.MemberType.ADMIN (패키지명 포함, 쿼리DSL 이나 파라미터 바인딩 사용하면 됨) 
- 엔티티 타입 : TYPE(m) = Member (상속 관계에서 사용)


##### 기타

- SQL과 문법이 같은 식
- EXISTS, IN
- AND, OR, NOT
- =, >, >=, <, <=, <>
- BETWEEN, LIKE, IS [NOT] NULL


---
## 조건식 - CASE 식

[image:42255C78-E756-4CD7-87FB-679E1B4CA2CD-85545-00015A96C7E6757F/스크린샷 2020-04-27 오후 2.33.01.png]

- COALESCE : 하나씩 조회해서 null이 아니면 반환
- NULLIF : 두 값이 같으면 null 반환, 다르면 첫번째 값 반환

사용자 이름이 없으면 이름 없는 회원을 반환
`select calesce(m.username, ‘이름 없는 회원’) from Member m`

사용자 이름이 ‘관리자’면 null을 반환하고 나머지는 본인 이름 반환
`select NULLIF(m.username, ‘관리자’) from Member m`


---
## JPQL 기본함수

- CONCAT (문자 더하기)

```
select concat(‘a’, ‘b’) from Member m
select ‘a’ || ‘b’ from Member m
```

- SUBSTRING (문자 자르기)

```
select substring(m.username, 2, 3) from Member m
```

- TRIM (다듬기)
- LOWER, UPPER (소문자, 대문자)
- LENGTH (길이)
- LOCATE ()

```
select locate(‘de’, ‘abcdefg’) from Member m 
// 4 (결과)
```

- ABS, SQRT, MOD (수 다루기)
- SIZE, INDEX (JPA 용도)

```
// 컬렉션 크기 반환
select size(t.members) from Team t
```


##### 사용자 정의 함수 호출

- 하이버네이트는 사용전 방언에 추가해야 함
	- 사용하는 DB 방언을 상속받고, 사용자 정의 함수를 등록한다
	`select function(‘group_concat’, i.name) from Item i`

```java
public class MyH2Dialect extends H2Dialect {
	public MyH2Dialect() {
		registerFunction(“group_concat”, new StandardSQLFunction(“group_concat”, StandardBasicTypes.STRING));
	}
}

// persistance.xml 에서 방언 변경
String query = “select function(‘group_concat’, m.username) from Member m”;

List<String> result = em.createQuery(query, String.class)
		.getResultList();
```


