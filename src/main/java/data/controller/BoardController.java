package data.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import data.dto.BoardDto;
import data.service.BoardService;
import data.service.UsersService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/board")
public class BoardController {

	@Autowired
	BoardService boardService;
	@Autowired
	UsersService userService;

	@GetMapping("/boardMain")
	public String boardMain(Model model,HttpSession session) {
		//세션에 저장된 사용자 id 받기
		int userId=(Integer)session.getAttribute("userId");
		
		List<BoardDto> boardList = boardService.getAllBoards();
		model.addAttribute("boardList", boardList);
		
		// 작성자 이름 매핑을 위한 Map 생성
	    Map<Integer, String> writerMap = new HashMap<>();
	    for (BoardDto dto : boardList) {
	        int authorId = dto.getAuthorId();
	        // 중복 조회 방지
	        if (!writerMap.containsKey(authorId)) {
	            String writerName = userService.readUserById(authorId).getName();
	            writerMap.put(authorId, writerName);
	        }
	    }
	    model.addAttribute("writerMap", writerMap);
		
		//임시 기본값 설정
	    model.addAttribute("currentPage", 1);
	    model.addAttribute("totalPages", 1);
		
		return "layout/boardMain";
	}

	@GetMapping("/boardWrite") 
	public String boardWrite(@RequestParam(value = "hidden", defaultValue = "false") 
	boolean hidden, Model model,HttpSession session) {
		//세션에 저장된 사용자 id 받기
		int userId=(Integer)session.getAttribute("userId");
		
		BoardDto boardDto = new BoardDto(); 
		boardDto.setHidden(hidden);
		model.addAttribute("board", boardDto); 
		return "layout/boardWrite"; 
	}
	  
	// 글쓰기 저장
	@PostMapping("/boardInsert")
	@ResponseBody
	public Map<String, Object> insertWrite(BoardDto dto,HttpSession session) {
		//System.out.println("=== 글쓰기 요청 ===");
		//System.out.println("카테고리: " + dto.getCategory());
		//System.out.println("hidden 값: " + dto.isHidden()); // 👈 여기!
		
		//세션에 저장된 사용자 id 받기
		int userId=(Integer)session.getAttribute("userId");
		
		// authorId 설정
	    dto.setAuthorId(userId);
		
		boolean success = boardService.boardInsert(dto);
		Map<String, Object> result = new HashMap<>();
		result.put("result", success ? 1 : 0);
		return result;
	}

	@GetMapping("/boardDetail/{id}")
	public String boardDetail(@PathVariable("id") int id, Model model,HttpSession session) {
		//세션에 저장된 사용자 id 받기
		int userId=(Integer)session.getAttribute("userId");
		
	    BoardDto board = boardService.getBoardDetailById(id); 
	    model.addAttribute("board", board);
	    model.addAttribute("isAuthorOrAdmin", true);

	    return "layout/boardDetail"; 
	}
	
//	// 글 상세보기
//	@GetMapping("/boardDetail/{id}")
//	public String boardDetail(@PathVariable int id, Model model) {
//		System.out.println("controller 1 >> id = " + id);
//		BoardDto boardDto = boardService.getBoardDetailById(id);
//		model.addAttribute("board", boardDto);
//		return "layout/boardDetail";
//	}

	@GetMapping("/boardList")
	public String boardList(@RequestParam(defaultValue = "1") int page, Model model) {
	    int pageSize = 10;
	    int offset = (page - 1) * pageSize;

	    List<BoardDto> list = boardService.getPagedBoardList(offset, pageSize);
	    int totalCount = boardService.getBoardCount();
	    int totalPages = (int) Math.ceil((double) totalCount / pageSize);

	    model.addAttribute("boardList", list);
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", totalPages);

	    return "layout/boardMain";
	}


	@GetMapping("/boardBlind")
	public String boardBlind(Model model,HttpSession session) {
		//세션에 저장된 사용자 id 받기
		int userId=(Integer)session.getAttribute("userId");
		List<BoardDto> hiddenList = boardService.getHiddenPosts();
		model.addAttribute("boardList", hiddenList);
		return "layout/boardBlind";
	}

	@GetMapping("/boardNoti")
	public String boardNoti(Model model,HttpSession session) {
		//세션에 저장된 사용자 id 받기
		int userId=(Integer)session.getAttribute("userId");		
		List<BoardDto> boardList = boardService.getBoardListByCategory("NOTICE");
		model.addAttribute("boardList", boardList);
		
		// 작성자 이름 매핑을 위한 Map 생성
	    Map<Integer, String> writerMap = new HashMap<>();
	    for (BoardDto dto : boardList) {
	        int authorId = dto.getAuthorId();
	        // 중복 조회 방지
	        if (!writerMap.containsKey(authorId)) {
	            String writerName = userService.readUserById(authorId).getName();
	            writerMap.put(authorId, writerName);
	        }
	    }
	    model.addAttribute("writerMap", writerMap);
		
		return "layout/boardNoti";
	}
	
	@GetMapping("/boardDepartment")
	public String boardDepartment(Model model,HttpSession session) {
		//세션에 저장된 사용자 id 받기
		int userId=(Integer)session.getAttribute("userId");
		List<BoardDto> boardList = boardService.getBoardListByCategory("DEPARTMENT");
		model.addAttribute("boardList", boardList);
		
		// 작성자 이름 매핑을 위한 Map 생성
	    Map<Integer, String> writerMap = new HashMap<>();
	    for (BoardDto dto : boardList) {
	        int authorId = dto.getAuthorId();
	        // 중복 조회 방지
	        if (!writerMap.containsKey(authorId)) {
	            String writerName = userService.readUserById(authorId).getName();
	            writerMap.put(authorId, writerName);
	        }
	    }
	    model.addAttribute("writerMap", writerMap);
		return "layout/boardDepartment";
	}
	
	@GetMapping("/noticeTop3")
	public ResponseEntity<Object> homeNoticeTop3() {
	    Map<String, Object> response = new LinkedHashMap<>();
	    try {
	        List<BoardDto> list = boardService.readNoticeTop3(); // ← 여기 수정!
	        response.put("status", "ok");
	        response.put("result", list);
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    } catch (Exception e) {
	        response.put("status", "error");
	        response.put("message", e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
}