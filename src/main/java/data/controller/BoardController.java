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
import org.springframework.web.bind.annotation.RequestBody;
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
		boolean success = boardService.boardInsert(dto);
		Map<String, Object> result = new HashMap<>();
		result.put("result", success ? 1 : 0);
		return result;
	}

	// 글 상세보기
	@GetMapping("/boardDetail/{id}")
	public String getBoardDetail(@PathVariable int id, Model model) {
		System.out.println("controller 1 >> id = " + id);
		BoardDto boardDto = boardService.getBoardDetailById(id);
		model.addAttribute("board", boardDto);
		return "layout/boardDetail";
	}

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
	    List<BoardDto> notiList = boardService.getNotiPosts();
	    model.addAttribute("boardList", notiList);
	    return "layout/boardNoti"; //공지사항
	}

	@GetMapping("/boardDepartment")
	public String boardDepartment(Model model) {
		List<BoardDto> boardList = boardService.getAllBoards();
		model.addAttribute("boardList", boardList);
		return "layout/boardDepartment";
	}
	


}