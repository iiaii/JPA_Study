# JPA 실습

이 코드는 실습예제이므로 일부 로직이나, 테스트코드가 없을 수 있습니다


본 프로젝트는 [# 고급 매핑] 실습 내용에서 다음과 같은 요구사항 추가되었습니다
* 모든 연관관계를 지연 로딩으로
* @ManyToOne, @OneToOne은 기본이 즉시 로딩이므로 지연 로딩으로 변경
* Order -> Delivery를 영속성 전이 ALL 설정
* Order -> OrderItem을 영속성 전이 ALL 설정