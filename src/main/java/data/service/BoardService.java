package data.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import data.dto.BoardDto;
import data.mapper.BoardMapper;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BoardService {
	@Autowired
	BoardMapper boardMapper;

	// 글쓰기 저장
	public boolean boardInsert(BoardDto boardDto) {
		int result = boardMapper.insertWrite(boardDto);
		return result > 0;
	}

	public int getTotalCount() {
		return boardMapper.getTotalCount();
	}

	public int getMaxIdx() {
		return boardMapper.getMaxIdx();
	}

	public void updateBoard(BoardDto dto) {
		boardMapper.updateBoard(dto);
	}

	public void deleteBoard(int idx) {
		boardMapper.deleteBoard(idx);
	}

	public List<BoardDto> getPagingList(int start, int perpage) {
		return boardMapper.getPagingList(start, perpage);
	}

	public BoardDto getSelectByIdx(int idx) {
		return boardMapper.getSelectByIdx(idx);
	}

	public List<BoardDto> getSelectById(String myid) {
		return boardMapper.getSelectById(myid);
	}

	public List<BoardDto> getAllBoards() {
		return boardMapper.getAllBoards();
	}

	public List<BoardDto> getHiddenPosts() {
		return boardMapper.selectHiddenPosts();
	}

	public List<BoardDto> getBoardListByCategory(String category) {
		return boardMapper.findByCategory(category);
	}

	public BoardDto getBoardById(int id) {
	    return boardMapper.findById(id);
	}

	public int updateBoard(BoardDto dto, MultipartFile[] files) {
		// TODO Auto-generated method stub
		return 0;
	}

}