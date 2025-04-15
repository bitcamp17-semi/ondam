package data.mapper;

import data.dto.BoardDto;

import java.util.List;

public interface IndexMapper {

    public List<BoardDto> readNoticeTop3();
}
