package com.wjc.codetest.product.controller;

import com.wjc.codetest.product.model.request.CreateProductRequest;
import com.wjc.codetest.product.model.request.GetProductListRequest;
import com.wjc.codetest.product.model.domain.Product;
import com.wjc.codetest.product.model.request.UpdateProductRequest;
import com.wjc.codetest.product.model.response.ProductListResponse;
import com.wjc.codetest.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
/*
- 문제 : 각 api 엔드포인트 url이 불규칙적입니다.(가독성)
- 원인 :
  - /get/product (동사 포함)
  - /create/product (동사 포함)
  - /update/product (동사 포함)
  - /delete/product (동사 포함)
  - /product/list (동사 미포함)
  - /product/category/list (동사 미포함)
- 개선안(대안) :
@RequestMapping("/products") 로 베이스 경로를 통일 하고
@GetMapping("/{productId}")과 같이 동사 대신 http메소드로 행동을 표현하도록 url을 수정해야 합니다.
개선 후 예시
GET    /products/{id}
POST   /products
PUT    /products/{id}
DELETE /products/{id}
GET    /products?category=전자제품&page=0&size=10
GET    /products/categories

- 선택 근거:
RESTful api naming 컨벤션 준수로 API 직관성이 좋아집니다.
HTTP 메서드로 액션을 구분할 수 있습니다.
 */
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping(value = "/get/product/by/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable(name = "productId") Long productId){
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @PostMapping(value = "/create/product")
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest dto){
        Product product = productService.create(dto);
        return ResponseEntity.ok(product);
    }

    @PostMapping(value = "/delete/product/{productId}") // 아래 updateProduct()와 같은 문제로 주석 하나로 대체합니다.
    public ResponseEntity<Boolean> deleteProduct(@PathVariable(name = "productId") Long productId){
        productService.deleteById(productId);
        return ResponseEntity.ok(true);
    }

    @PostMapping(value = "/update/product")
    public ResponseEntity<Product> updateProduct(@RequestBody UpdateProductRequest dto){
        Product product = productService.update(dto);
        return ResponseEntity.ok(product);
    }
    /*
    - 문제 : http 메소드를 잘못 사용하고 있습니다.
    - 원인 : delete, update 작업에 PostMapping을 사용하고 있습니다. post 메소드는 비멱등 메소드로 새로운 리소스를 생성할 때 사용합니다.
    - 개선안(대안) :
    조회 할 때 @GetMapping,
    리소스를 생성 할 때 @PostMapping,
    리소스를 삭제할 때 @DeleteMapping,
    수정이 필요할 때 @PutMapping을 사용 합니다.
    - 선택 근거 :
    적절한 http 메소드를 사용해 api 의도가 명확해집니다.
    또 업데이트 기능과 같이 멱등한 연산을 할 경우 post 보다 put을 사용하는 것이 의미적으로 적절하다고 생각합니다.
     */
    @PostMapping(value = "/product/list")
    public ResponseEntity<ProductListResponse> getProductListByCategory(@RequestBody GetProductListRequest dto){
        Page<Product> productList = productService.getListByCategory(dto);
        return ResponseEntity.ok(new ProductListResponse(productList.getContent(), productList.getTotalPages(), productList.getTotalElements(), productList.getNumber()));
    }
    /*
    - 문제 : http 메소드를 잘못 사용하고 있습니다. 또, 코드 한줄의 길이가 너무 긴듯 합니다.
    - 원인 : 조회 목적 api 입니다만 post 메소드에 RequestBody를 통해 요청을 받고 있고, 화면을 벗어날 만큼 코드 길이가 깁니다.
    - 개선안 :
    @GetMapping
    public ResponseEntity<ProductListResponse> getProductListByCategory(
        @RequestParam(value = "category") String category,
        @RequestParam(value = "page") int page,
        @RequestParam(value = "size") int size
    )
    로 get http method를 사용하면서 쿼리스트링을 이용해 요청하고 적당히 줄바꿈 하는 것이 좋을것 같습니다.
    - 선택 근거 :
    코드가 너무길어지면 가독성이 안좋아지기 때문에 줄바꿈을 하는 것이 좋다고 생각합니다.
    또, 단순 특정 카테고리의 상품들을 조회하는 api라고 보여집니다.
    따라서 조회 조건이 너무 복잡한 경우도 아니고 노출되지 말아야할 민감한 정보가 아니라고 생각 돼 쿼리스트링과 get 메소드를 사용하는 것이
    좋다고 생각합니다.
     */

    @GetMapping(value = "/product/category/list")
    public ResponseEntity<List<String>> getProductListByCategory(){
        List<String> uniqueCategories = productService.getUniqueCategories();
        return ResponseEntity.ok(uniqueCategories);
    }
}
/*
- 문제 : 메서드에서 불필요한 변수를 사용해 코드 퀄리티 저하
- 원인 : getProductListByCategory를 제외하면 불필요한 중간 저장
- 개선안 :
단순 반환 시 return ResponseEntity.ok(service.method()) 형식으로 사용 하되,
getProductListByCategory처럼 후처리가 필요할 때만 변수에 담아 사용하면 좋을 것 같습니다.
- 선택 근거 :
현재는 getProductListByCategory를 제외하면
전부 사용하지 않는 변수에 담아져 있기 때문에 즉시 반환 하는 방법을 사용하는 것이 더 깔끔하다고 생각합니다.


크리티컬한 문제 - controller entity 반환

- 문제 : 클라이언트가 필요하지 않는 정보도 함께 제공 될 수 있고, entity간 연관 관계 매핑시 레이지 로딩 이슈가 발생할 수 있습니다.
- 원인 : entity를 직접 반환하고 있고, 특정 카테고리 상품목록 api 에서 new ProductListResponse(productList.getContent() 사용
- 개선안 :
controller 에서 Response에 필요한 property만 갖는 dto를 생성 하고
Service에서는 Contoller에게 제공할 dto를 생성합니다.
- 선택 근거 :
현재는 product entity 내부에 property가 category와 name만 있지만 여러 민감한 정보가 생길 수 있으며, 사용자에게 설계를 노출 하게 됩니다.
어떠한 entity도 직접 반환에 사용되는 것이 아닌 비즈니스로직에 집중 되어야 하고 필요한 데이터는 dto를 통해 노출시켜야 합니다.

service layer에서는 지연 로딩 이슈를 방지하고자 영속성 상태가 끝나기 전 컨트롤러에게 전달할 dto를 생성해 반환 해주고
컨트롤러는 해당 dto를 사용자 니즈에 맞게 가공해 제공하여 문제를 해결할 수 있습니다.
 */
