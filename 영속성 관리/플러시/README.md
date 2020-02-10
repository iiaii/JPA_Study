# #JPA/플러시

## 플러시

영속성 컨텍스트의 변경내용을 데이터베이스에 반영


## 플러시 발생

- 변경 감지
- 수정된 엔티티 쓰기 지연 SQL 저장소에 등록
- 쓰기 지연 SQL 저장소의 쿼리를 데이터 베이스에 전송 (등록, 수정, 삭제)

(플러시를 한다고 데이터 베이스 트랜잭션이 커밋 되는 것이 아님)


## 영속성 컨텍스트 플러시 하는 방법

- em.flush() - 직접 호출

```java
EntityManaber em = emf.createEntityManager();
EntityTransaction transaction = em.getTransaction();
transaction.begin(); // 트랜잭션 시작

Member member = new Member(200L,”member200”);
em.persist(member);

em.flush();	// 쿼리가 디비에 바로 반영

transaction.commit();
```

- 트랜잭션 커밋 - 플러시 자동 호출
- JPQL 쿼리 실행 - 플러시 자동 호출

```java
em.persist(memberA);
em.persist(memberB);
em.persist(memberC);

// 중간에 JPQL 실행 (조회할때 데이터 가져올수 있음)
// 플러시 모드 옵션도 있음 (그냥 쓰는걸 권장)
query = em.createQuery(“select m from Member as m”, Member.class);
List<Member> members = query.getResultList();
```


## 정리

#### 플러시는 ..
- 영속성 컨텍스트를 비우지 않음
- 영속성 컨텍스트의 변경 내용을 데이터베이스에 동기화하는 것임
- 트랜잭션이라는 작업 단위가 중요 -> 커밋 직전에만 동기화 하면 됨


 