package data.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
@RequestMapping("/board")
public class BoardController {

	@Autowired
	BoardService boardService;

	@GetMapping("/boardMain")
	public String boardMain(Model model) {
		List<BoardDto> boardList = boardService.getAllBoards();
		model.addAttribute("boardList", boardList);
		
		//임시 기본값 설정
	    model.addAttribute("currentPage", 1);
	    model.addAttribute("totalPages", 1);
		
		return "layout/boardMain";
	}

	@GetMapping("/boardWrite") 
	public String boardWrite(@RequestParam(value = "hidden", defaultValue = "false") 
	boolean hidden, Model model) { 
		BoardDto boardDto = new BoardDto(); 
		boardDto.setHidden(hidden);
		model.addAttribute("board", boardDto); 
		return "layout/boardWrite"; 
	}
	  
	// 글쓰기 저장
	@PostMapping("/boardInsert")
	@ResponseBody
	public Map<String, Object> insertWrite(BoardDto dto) {
		//System.out.println("=== 글쓰기 요청 ===");
		//System.out.println("카테고리: " + dto.getCategory());
		//System.out.println("hidden 값: " + dto.isHidden()); // 👈 여기!
		
		boolean success = boardService.boardInsert(dto);
		Map<String, Object> result = new HashMap<>();
		result.put("result", success ? 1 : 0);
		return result;
	}

	@GetMapping("/boardDetail/{id}")
	public String boardDetail(@PathVariable("id") int id, Model model) {
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
	public String boardList(Model model) {
		List<BoardDto> list = boardService.getAllBoards();
		model.addAttribute("boardList", list);
		return "layout/boardMain";
	}

	@GetMapping("/boardBlind")
	public String boardBlind(Model model) {
		List<BoardDto> hiddenList = boardService.getHiddenPosts();
		model.addAttribute("boardList", hiddenList);
		return "layout/boardBlind";
	}

	@GetMapping("/boardNoti")
	public String boardNoti(Model model) {
		List<BoardDto> boardList = boardService.getBoardListByCategory("NOTICE");
		model.addAttribute("boardList", boardList);
		return "layout/boardNoti";
	}
	
	@GetMapping("/boardDepartment")
	public String boardDepartment(Model model) {
		List<BoardDto> boardList = boardService.getBoardListByCategory("DEPARTMENT");
		model.addAttribute("boardList", boardList);
		return "layout/boardDepartment";
	}
}