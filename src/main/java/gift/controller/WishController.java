package gift.controller;

import gift.domain.AuthToken;
import gift.dto.request.WishCreateRequest;
import gift.dto.request.WishDeleteRequest;
import gift.dto.request.WishEditRequest;
import gift.dto.response.WishResponseDto;
import gift.service.TokenService;
import gift.service.WishService;
import gift.utils.PageableUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wishes")
public class WishController {
    private final TokenService tokenService;
    private final WishService wishService;

    public WishController(TokenService tokenService, WishService wishService) {
        this.tokenService = tokenService;
        this.wishService = wishService;
    }

    @GetMapping
    public ResponseEntity<List<WishResponseDto>> getWishProducts(HttpServletRequest request,
                                                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                                                 @RequestParam(name = "size", defaultValue = "10") int size,
                                                                 @RequestParam(name = "sort", defaultValue = "createdDate,desc") String sortBy
    ){
        AuthToken token = getAuthVO(request);
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy);
        List<WishResponseDto> findProducts = wishService.findWishesPaging(token.getEmail(), pageable);

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(findProducts);
    }

    @PostMapping
    public ResponseEntity<WishResponseDto> addWishProduct(HttpServletRequest request,
                                                          @RequestBody @Valid WishCreateRequest wishCreateRequest){
        AuthToken token = getAuthVO(request);

        WishResponseDto wishResponseDto = wishService.addWish(wishCreateRequest.product_id(), token.getEmail(), wishCreateRequest.count());

        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(wishResponseDto);
    }

    @PutMapping
    public String editWishProduct(HttpServletRequest request,
                                  @RequestBody @Valid WishEditRequest wishEditRequest){
        AuthToken token = getAuthVO(request);
        wishService.editWish(wishEditRequest.wish_id(), token.getEmail() , wishEditRequest.count());
        return "redirect:/wishes";
    }

    @DeleteMapping("/{wishId}")
    public ResponseEntity<WishResponseDto> deleteLikesProduct(HttpServletRequest request,
                                                              @PathVariable("wishId") Long wishId){
        AuthToken token = getAuthVO(request);
        WishResponseDto wishResponseDto = wishService.deleteWish(wishId, token.getEmail());
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(wishResponseDto);
    }

    public AuthToken getAuthVO(HttpServletRequest request) {
        String key = request.getHeader("Authorization").substring(7);
        AuthToken token = tokenService.findToken(key);
        return token;
    }

}
