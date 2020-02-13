# JPA 실습

이 코드는 실습예제이므로 일부 로직이나, 테스트코드가 없을 수 있습니다


### 조인 전략으로 상속관계의 테이블을 구성하였습니다
* BaseEntity (상속관계와 상관없음, 모든 속성을 포함 @MappedSuperclass)
* Item (조상) : @Inheritance(strategy = InheritanceType.JOINED)
* Album (자식)
* Book (자식)
* Movie (자식)


### 주의

persistence.xml 에서 `<property name="hibernate.hbm2ddl.auto" value="create" />` 로 설정했으나 상속관계에 의해 Drop table 쿼리가 Item 부터 나가는 경우 에러가 발생할 수 있습니다.



