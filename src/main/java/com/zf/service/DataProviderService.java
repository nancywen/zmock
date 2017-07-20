package com.zf.service;

import com.alibaba.fastjson.JSON;
import com.zf.dao.domain.MockInfoDao;
import com.zf.tool.Constants;
import com.zf.utils.PropUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataProviderService {

    @PostConstruct
    public void initial(){
        this.loadMockData();
    }

    public static ConcurrentHashMap<String, List<String>> MOCK_COLLECTIONS = new ConcurrentHashMap<>();

	public static ConcurrentHashMap<String, MockInfoDao> MOCK_DATAS = new ConcurrentHashMap<>();

	public static String MOCK_DATAS_KEY_CONNECTOR = "+";

    public void loadMockData(){
	    try{
	    	
	    	//从配置文件读取路径 --避免每次重启后都需要配置一遍
	    	String dataDirInConfig = PropUtil.getString(Constants.MOCK_DATA_DIR);
	    	
	        if(StringUtils.isBlank(DataSettingService.mockDataDir) && StringUtils.isBlank(dataDirInConfig)){
	            return;
            }else if(StringUtils.isBlank(DataSettingService.mockDataDir) && StringUtils.isNotBlank(dataDirInConfig) ){
            	DataSettingService.mockDataDir = dataDirInConfig;
            }
            MOCK_COLLECTIONS.clear();
            MOCK_DATAS.clear();
            File file = new File(DataSettingService.mockDataDir);
            if(!file.exists()){
                file.mkdirs();
            }
            File[] collections = file.listFiles();
            for (File collection : collections) {
                if (!MOCK_COLLECTIONS.containsKey(collection.getName())){
                    MOCK_COLLECTIONS.put(collection.getName(), new ArrayList<String>());
                }
                File[] mockNames = collection.listFiles();
                for (File mockName : mockNames) {
                    String fileName = mockName.getName();
                    String mn = fileName.substring(0, fileName.lastIndexOf('.'));
                    MOCK_COLLECTIONS.get(collection.getName()).add(mn);
                    String key = collection.getName()+MOCK_DATAS_KEY_CONNECTOR+mn;
                    String mockData = this.readFile(DataSettingService.mockDataDir + File.separator + collection.getName() + File.separator + mockName.getName());
                    MockInfoDao mockInfo = new MockInfoDao();
                    try{
                        if(StringUtils.isNotBlank(mockData)){
                            mockInfo = JSON.parseObject(mockData, MockInfoDao.class);
                        }
                    }catch(Exception e){
                        //
                    }
                    MOCK_DATAS.put(key, mockInfo);
                }
            }
        }catch(Exception e){
	        e.printStackTrace();
        }
    }

    private String readFile(String filePath){
        InputStream in = null;
        ByteArrayOutputStream bos = null;
        try {
            in = new FileInputStream(filePath);
            bos = new ByteArrayOutputStream();
            int read;
            while((read= in.read())!=-1){
                bos.write(read);
                bos.flush();
            }
            return bos.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(in != null){
                    in.close();
                }
                if(bos != null){
                    bos.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }
	
}
