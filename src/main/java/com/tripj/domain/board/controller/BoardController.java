package com.tripj.domain.board.controller;

import com.tripj.domain.board.model.dto.request.CreateBoardRequest;
import com.tripj.domain.board.model.dto.request.GetBoardRequest;
import com.tripj.domain.board.model.dto.request.GetBoardSearchRequest;
import com.tripj.domain.board.model.dto.response.*;
import com.tripj.domain.board.service.BoardService;
import com.tripj.global.code.ErrorCode;
import com.tripj.global.model.RestApiResponse;
import com.tripj.resolver.userinfo.UserInfo;
import com.tripj.resolver.userinfo.UserInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/board")
@Tag(name = "board", description = "게시글 API")
public class BoardController {

    private final BoardService boardService;

    @Operation(
            summary = "게시글 등록 API",
            description = "게시글을 등록합니다."
    )
    @PostMapping("")
    public RestApiResponse<CreateBoardResponse> createBoard(
            @UserInfo UserInfoDto userInfo,
            @Validated @RequestPart CreateBoardRequest request,
            @RequestPart(required = false, value = "images") List<MultipartFile> images,
            BindingResult bindingResult) throws IOException {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return RestApiResponse.error(ErrorCode.E400_BINDING_RESULT, errorMessage);
        }

        return RestApiResponse.success(
                boardService.createBoard(request, userInfo.getUserId(), images));
    }

    @Operation(
            summary = "게시글 수정 API",
            description = "게시글을 수정합니다."
    )
    @PostMapping("/{boardId}")
    public RestApiResponse<CreateBoardResponse> updateBoard(
            @PathVariable Long boardId,
            @UserInfo UserInfoDto userInfo,
            @Validated @RequestPart CreateBoardRequest request,
            @RequestPart(required = false, value = "images") List<MultipartFile> images,
            BindingResult bindingResult) throws IOException {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return RestApiResponse.error(ErrorCode.E400_BINDING_RESULT, errorMessage);
        }

        return RestApiResponse.success(
                boardService.updateBoard(request, boardId, userInfo.getUserId(), images));
    }

    @Operation(
            summary = "게시글 삭제 API",
            description = "게시글을 삭제합니다."
    )
    @DeleteMapping("/{boardId}")
    public RestApiResponse<DeleteBoardResponse> deleteBoard(
            @UserInfo UserInfoDto userInfo,
            @PathVariable Long boardId) {

        return RestApiResponse.success(
                boardService.deleteBoard(userInfo.getUserId(), boardId));
    }

    @Operation(
            summary = "게시글 상세 조회 API",
            description = "게시글을 상세조회 합니다."
    )
    @GetMapping("/{boardId}")
    public RestApiResponse<GetBoardDetailResponse> getBoardDetail(
            @PathVariable Long boardId) {

        return RestApiResponse.success(
                boardService.getBoardDetail(boardId));
    }

    @Operation(
            summary = "게시글 상세 댓글 조회 API",
            description = "게시글 상세조회시 댓글을 조회 합니다."
    )
    @GetMapping("/{boardId}/comments")
    public RestApiResponse<List<GetBoardCommentResponse>> getBoardComment(
            @PathVariable Long boardId) {

        return RestApiResponse.success(
                boardService.getBoardComment(boardId));
    }

    @Operation(
            summary = "게시글 리스트 조회 API",
            description = "게시글(후기,질문,꿀팁) 리스트 조회 무한스크롤 합니다."
    )
    @GetMapping("/scroll")
    public RestApiResponse<Slice<GetBoardResponse>> getBoardListScroll(
            @RequestParam(required = false) Long lastBoardId,
            @PageableDefault(size = 5) Pageable pageable) {

        GetBoardRequest request = GetBoardRequest.of(lastBoardId);
        Slice<GetBoardResponse> getBoardList =
                boardService.getBoardListScroll(request, pageable);

        return RestApiResponse.success(getBoardList);
    }

    @Operation(
            summary = "게시글 리스트 조회 API",
            description = "게시글(후기,질문,꿀팁) 리스트 조회 합니다."
    )
    @GetMapping("")
    public RestApiResponse<List<GetBoardResponse>> getBoardList(
            @RequestParam Long boardCateId) {

        return RestApiResponse.success(boardService.getBoardList(boardCateId));
    }

    @Operation(
            summary = "최신글 리스트 조회 API",
            description = "최신글 리스트 조회 합니다."
    )
    @GetMapping("/latest")
    public RestApiResponse<List<GetBoardResponse>> getBoardLatestList() {

        return RestApiResponse.success(boardService.getBoardLatestList());
    }

    @Operation(
            summary = "인기글 리스트 조회 API",
            description = "인기글 리스트 조회 합니다."
    )
    @GetMapping("/popular")
    public RestApiResponse<List<GetBoardResponse>> getBoardPopularList() {

        return RestApiResponse.success(boardService.getBoardPopularList());
    }

    @Operation(
            summary = "게시글 전체 검색 조회 API",
            description = "게시글에서 전체 검색을 합니다."
    )
    @GetMapping("/all")
    public RestApiResponse<List<GetBoardResponse>> getAllBoardList(
            @Parameter GetBoardSearchRequest request) {

        return RestApiResponse.success(boardService.getAllBoardList(request));
    }

    @Operation(
            summary = "내가 쓴 게시글 조회 API",
            description = "마이페이지에서 내 게시글을 조회 합니다."
    )
    @GetMapping("/my/{userId}")
    public RestApiResponse<List<GetBoardResponse>> getMyBoardList(
            @UserInfo UserInfoDto userInfo) {

        return RestApiResponse.success(
                boardService.getMyBoardList(userInfo.getUserId()));
    }

    @Operation(
            summary = "내가 좋아요 누른 게시글 조회 API",
            description = "마이페이지에서 내 좋아요 조회 합니다."
    )
    @GetMapping("/my/liked/{userId}")
    public RestApiResponse<List<GetBoardResponse>> getMyLikedBoard(
            @UserInfo UserInfoDto userInfo) {
        return RestApiResponse.success(
                boardService.getMyLikedBoard(userInfo.getUserId()));
    }


}
