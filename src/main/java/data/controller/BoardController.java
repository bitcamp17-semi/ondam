package data.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
		return "layout/boardMain";
	}

	@GetMapping("/boardWrite")
	public String boardWrite(@RequestParam(value = "hidden", defaultValue = "false") boolean hidden, Model model) {
		BoardDto boardDto = new BoardDto();
		boardDto.setHidden(hidden);
		model.addAttribute("board", boardDto);
		return "layout/boardWrite";
	}

	@PostMapping("/boardInsert")
	@ResponseBody
	public Map<String, Object> insertWrite(BoardDto dto) {
		boolean success = boardService.boardInsert(dto);
		Map<String, Object> result = new HashMap<>();
		result.put("result", success ? 1 : 0);
		return result;
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
		return "layout/blindBoard";
	}

	@GetMapping("/boardUpdate")
	public String boardUpdate(@RequestParam("id") int id, Model model) {
		BoardDto board = boardService.getBoardById(id);
		model.addAttribute("board", board);
		return "layout/boardUpdate";
	}

	@GetMapping("/boardEvent")
	public String boardEvent(Model model) {
		List<BoardDto> list = boardService.getBoardListByCategory("사내소식");
		model.addAttribute("list", list);
		model.addAttribute("totalCount", list.size());
		return "boardEvent"; //
	}
	
	 @GetMapping("/boardnoti")
	    public String showNoticeBoard(Model model) {
	        
	    return "board/boardnoti";
	    }
}