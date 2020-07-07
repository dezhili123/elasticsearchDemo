package com.njtdg.controller;

import com.njtdg.service.ContentService;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 请求编写
 */
@RestController
public class ContentController {

    @Autowired
    private ContentService contentService;

    @GetMapping(value = "/parse/{keywords}")
    public Boolean parse(@PathVariable("keywords") String keywords) throws IOException {
        return contentService.parseContent(keywords);
    }

    @GetMapping(value = "/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String,Object>> searchPage(@PathVariable("keyword")String keyword,
                                               @PathVariable("pageNo")Integer pageNo,
                                               @PathVariable("pageSize")Integer pageSize) throws IOException {
        return contentService.searchPage(keyword,pageNo,pageSize);
    }



}
