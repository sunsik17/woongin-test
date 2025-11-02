package com.wjc.codetest.product.service;

import com.wjc.codetest.product.model.request.CreateProductRequest;
import com.wjc.codetest.product.model.request.GetProductListRequest;
import com.wjc.codetest.product.model.domain.Product;
import com.wjc.codetest.product.model.request.UpdateProductRequest;
import com.wjc.codetest.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product create(CreateProductRequest dto) {
        Product product = new Product(dto.getCategory(), dto.getName());
        return productRepository.save(product);
    }

    public Product getProductById(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {
            throw new RuntimeException("product not found");
            // globalExceptionHandler 에서 리뷰
        }
        return productOptional.get();
    }
    /*
    - 문제 : Optional사용 방식
    - 원인 : isPresent() 이후 get() 사용
    - 개선안 :
    return productRepository.findById(productId).orElseThrow(
      () -> new RuntimeException("product not found");
    );
    - 선택 근거 :
    null check와 exception을 한줄로 표현할 수 있어 간결하고 명확합니다.
     */

    public Product update(UpdateProductRequest dto) {
        Product product = getProductById(dto.getId());
        product.setCategory(dto.getCategory());
        product.setName(dto.getName());
        Product updatedProduct = productRepository.save(product);
        return updatedProduct;

    }
    /*
    - 문제 1: 공백
    - 원인 : return updateProduct; 아래 의미 없는 줄바꿈
    - 개선안 : 삭제
    - 선택 근거 :
    팀단위 프로젝트라면 팀 코드 컨벤션이 있을 것이고 해당 컨벤션을 지켜주어야 합니다.
    필요 없는 공백을 없애고 코드 컨벤션을 지키려는 습관을 들여야 합니다.

    - 문제 2: 잘못된 코드 재사용, 불필요한 변수
    - 원인 : 직접 repository를 이용하지 않고 같은 service class내에 있는 메소드 getProductById()를 재사용 중입니다.
    - 개선안 :
    같은 클래스의 getProductById()보다 직접 repository를 사용해 findById()를 사용하는 것이 좋습니다.
    updatedProduct같이 사용하지 않는 변수는 삭제하여
    return productRepository.save(product); 로 단순화 해주세요
    - 선택 근거:
    repository 직접 호출로 데이터 접근 로직이 명확해집니다.
    중간 변수 제거로 코드 가독성이 좋아집니다.
    */
    public void deleteById(Long productId) {
        Product product = getProductById(productId);
        productRepository.delete(product);
    }
    /*
    - 문제 : 불필요한 내부 메소드 호출
    - 원인 : getProductById()를 사용해 의미 있는 재사용처럼 보이지만, 해당 메소드는 repository.findById()로 대체 할 수 있습니다.
    - 개선안 :
    Optional객체를 반환하는 repository.findById().orElseThrow()를 이용해 의도한 에러메세지를 보여주고 delete()를 사용하거나
    기능만을 위한 것이라면 repository.deleteById()를 이용해 예외처리를 맡기는 것이 나아 보입니다.
    - 트레이드오프:
    repository.deleteById()를 이용하면 코드 한줄로 조회 삭제 로직이 가능합니다만
    데이터를 찾지 못하면 SimpleJpaRepository에서 고정으로 예외가 던져지게 되고
    findById() + delete()를 이용하면 예외처리를 커스텀할 수 있다는 장점이 있습니다.
     */

    public Page<Product> getListByCategory(GetProductListRequest dto) {
        PageRequest pageRequest = PageRequest.of(dto.getPage(), dto.getSize(), Sort.by(Sort.Direction.ASC, "category"));
        return productRepository.findAllByCategory(dto.getCategory(), pageRequest);
    }
    /*
    -문제 : 성능 낭비
    -원인 : findAllByCategory()는 이미 특정 카테고리의 상품들을 찾아 내지만 category로 정렬하는 효과 없는 정렬쿼리 발생
    -개선안 :
    pageRequest 객체를 생성할 때 Sort.by를 삭제하거나
    서비스가 아닌 클라이언트가 선택한 기준에 맞게 정렬할 수 있도록 해당 코드를 사용하는 controller에 request param으로 기준을 추가해
    Pageable 객체를 전달 받습니다.
    -선택근거 :
    개발자가 직접 코드로 category를 이용한 정렬을 진행하기 때문에
    의미 없는 정렬 쿼리 ORDER BY 줄이 사라지거나 의미있게 정렬 할 수 있도록 바뀌게 됩니다.
    또, 해당 서비스 코드는 핵심 로직만을 담당할 수 있어 단위테스트에 용이합니다.
     */

    public List<String> getUniqueCategories() {
        return productRepository.findDistinctCategories();
    }
}

/*
문제 : 전체 적인 코드 퀄리티
원인 : 생성자를 통한 객체 생성
개선안 : 디자인 패턴 중 빌더 패턴 채택
선택 근거 :
생성하는 객체의 속성이 많을 수록 순서를 외워야 하는 번거로움이 사라집니다.
같은 타입이지만 전혀 다른 속성이 잘못 입력되는 문제를 방지할 수 있습니다.
트레이드 오프 :
코드가 증가 하지만, 속성이 추가 수정 되는 등의 문제에서 유지보수에 용이 합니다.
 */
