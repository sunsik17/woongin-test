package com.wjc.codetest.product.repository;

import com.wjc.codetest.product.model.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAllByCategory(String name, Pageable pageable);
    /*
    - 문제 : 가독성 문제
    - 원인 : 쿼리에는 문제가 없지만, 첫 번째 파라미터 String name이 어떤 의미인지 명확하지 않음
    - 개선안 :
    String category 으로 변경
    - 선택 근거 :
    메서드명과 파라미터명이 일치하면 코드의 의도를 더 직관적으로 전달할 수 있습니다.
    다른 개발자가 봐도 'category를 기준으로 상품을 조회한다'는 것을 바로 이해할 수 있습니다.

    - 문제 2: 성능
    - 원인 : Page객체를 반환하기 위해 hibernate가 count쿼리를 발생
    - 개선안:
    대용량 데이터가 된 다면
    offset, limit을 이용한 페이지네이션 -> where, limit 이용한 페이지네이션으로 변경
    - 선택 근거 :
    Page객체를 반환하기 위해 hibernate가 조회 할때마다 count쿼리를 발생시켜 비효율적입니다.
    category로 필터링한 모든 product를 조회하는 것이기 때문에, 서비스가 커지면 당연히 대용량데이터가 될 듯 합니다.
    수 백만건의 데이터중 마지막 페이지를 조회하려면 모든 데이터를 스캔해야 하기 때문에 성능상 문제가 예상 됩니다.
    트레이드 오프 :
    cursor 기반            <->         offset 기반
    ui 제공 불가                        ui 제공 가능
    초기비용 발생                       구현 쉬움
    요청마다 성능 무관                   뒤 페이지 갈수록 느려짐 (최적화 필요)
     */

    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findDistinctCategories();
}
