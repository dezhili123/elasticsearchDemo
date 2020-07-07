package com.example;

import com.alibaba.fastjson.JSON;
import com.example.pojo.User;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.lang.model.element.VariableElement;
import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class NjtdgSeApiApplicationTests {

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    //测试索引的创建  Request
    @Test
    void testCreateIndex() throws IOException {
        //1、创建索引的请求
        CreateIndexRequest request = new CreateIndexRequest("njtdg_index");
        //2、客户端执行请求，请求后获得响应
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }

    //测试获取索引
    @Test
    void testExistIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest("njtdg_index");
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    //测试删除索引
    @Test
    void testDeletIndex() throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("njtdg_index");
        AcknowledgedResponse delete = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println(delete);
    }

    //测试添加文档
    @Test
    void testAddDocument() throws IOException {
        //创建对象
        User user = new User("张三",3);
        //创建请求
        IndexRequest request = new IndexRequest("njtdg_index");

        //规则 put /kuang_index/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));

        //将我们的数据放入请求  json
        IndexRequest source = request.source(JSON.toJSONString(user), XContentType.JSON);

        //客户端发送请求,获取响应的结果
        IndexResponse index = client.index(request, RequestOptions.DEFAULT);

        System.out.println(index.toString());
        System.out.println(index.status());  //对应我们命令返回的状态  CREATED
    }


    //获取文档,判断是否存在 get/index/doc/1
    @Test
    void testIsExists() throws IOException {
        GetRequest getRequest = new GetRequest("njtdg_index", "1");
        //不获取返回的 _source的上下文了
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");

        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }


    //获取文档的信息
    @Test
    void testGetDocument() throws IOException {
        GetRequest getRequest = new GetRequest("njtdg_index", "1");
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(getResponse.getSourceAsString()); //打印文档的内容
        System.out.println(getResponse);  //返回的全部内容和命令是一样的
    }

    //更新文档的信息
    @Test
    void testUpdateRequest() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("njtdg_index", "1");
        updateRequest.timeout("1s");

        User user = new User("李四", 18);
        updateRequest.doc(JSON.toJSONString(user),XContentType.JSON);

        UpdateResponse update = client.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(update.status());
    }

    //删除文档记录
    @Test
    void testDeleteRequest() throws IOException {
        DeleteRequest request = new DeleteRequest("njtdg_index", "1");
        request.timeout("1s");

        DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.status());
    }

    //批量插入数据
    @Test
    void testBatchRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");

        ArrayList<User> userArrayList = new ArrayList<>();
        userArrayList.add(new User("zs1",3));
        userArrayList.add(new User("zs2",4));
        userArrayList.add(new User("zs3",5));
        userArrayList.add(new User("zs4",6));
        userArrayList.add(new User("zs5",7));
        userArrayList.add(new User("zs6",8));

        userArrayList.stream().forEach(x->{
            bulkRequest.add(new IndexRequest("njtdg_index")
            .id(x.getAge()+"")
            .source(JSON.toJSONString(x),XContentType.JSON));
        });

        BulkResponse responses = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(responses.hasFailures());  //是否失败,返回false代表成功
    }

    //查询
    // SearchRequest 搜索请求
    // SearchSourceBuilder 条件构造
    // HighlightBuilder 构建高亮
    // TermQueryBuilder 精确查询
    // MathcAllQueryBuilder
    @Test
    void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("njtdg_index");
        //构建搜索的条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //查询条件，我们可以使用QueryBuilders 工具来实现
        //QueryBuilders.termQuery 精确
        //QueryBuilders.matchAllQuery() 匹配所有
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "zs1");
        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        searchRequest.source(searchSourceBuilder);
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(search.getHits()));
        System.out.println("===================================");
        for (SearchHit documentFields:search.getHits().getHits()){
            System.out.println(documentFields.getSourceAsMap());
        }
    }

}
