# #JPA/준영속상태

## 준영속 상태

- 영속 -> 준영속
- 영속 상태의 엔티티가 영속성 컨텍스트에서 분리 (detached)
- 영속성 컨텍스트가 제공하는 기능을 사용 못함

1차 캐시에 올라간 상태가 영속상태임
-> JPA가 관리하는 상태
(persist이외에도 find 했을때)


## 준영속 상태로 만드는 방법

- em.detach(entity) : 특정 엔티티만 준영속 상태로 전환
- em.clear() : 영속성 컨텍스트를 완전 초기화
- em.close() : 영속성 컨텍스트를 종료

```java
Member member = em.find(Member.class, 150L); // 바로쿼리나감
member.setName(“AAA”);

em.detach(member);	// JPA 가 더이상 관리하지 않음 (업데이트 쿼리 없음)
// em.clear(); // 통으로 초기화 

tx.commit();
```





