package com.github.avarabyeu.guicyspark.service;

import com.github.avarabyeu.guicyspark.service.model.Validation;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * DAO for Validation table
 *
 * @author Andrei Varabyeu
 */
public interface ValidationDao {

    @Select("SELECT * FROM validation WHERE id = #{id}")
    Validation findById(int id);

    @Select("SELECT * FROM validation WHERE status = #{status}")
    List<Validation> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM validation WHERE status = #{status} LIMIT #{limit}")
    List<Validation> findByStatus(@Param("status") String status, @Param("limit") int maxItems);

    @Select("SELECT * FROM validation")
    List<Validation> findAll();

    @Insert("INSERT INTO validation(date,url,status) VALUES (#{date}, #{url}, #{status})")
    @SelectKey(statement = "call identity()", keyProperty = "id", before = false, resultType = Integer.class)
    int insertValidation(Validation validation);

    /* it's better to update only needed field. added just as example */
    @Update("UPDATE validation SET date=#{date}, url=#{url}, status=#{status}, error=#{error} WHERE id=#{id}")
    int updateValidation(Validation validation);


}
