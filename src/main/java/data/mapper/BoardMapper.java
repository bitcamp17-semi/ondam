package data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import data.dto.BoardDto;

@Mapper
public interface BoardMapper {
	public int insertWrite(BoardDto dto); //글쓰기 저장
	
	//public BoardDto getUserId(String userId);
	public List<BoardDto> getAllBoards();

	public int getMaxIdx();
	public void updateRestep(int regroup,int restep);
	
	public List<BoardDto> getPagingList(int start,int perpage);
	public BoardDto getSelectByIdx(int idx);
	public List<BoardDto> getSelectById(String myid);
	public void updateBoard(BoardDto dto);
	public void deleteBoard(int idx);
	public List<BoardDto> selectAllPosts();
	public void insertPost(BoardDto dto);
	public int getTotalCount();
	
	public BoardDto findById(int id);
	public List<BoardDto> findByCategory(String category);
	public List<BoardDto> selectHiddenPosts();
	
}