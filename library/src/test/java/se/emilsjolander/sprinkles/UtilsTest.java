package se.emilsjolander.sprinkles;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Arrays;

import se.emilsjolander.sprinkles.exceptions.NoTableAnnotationException;

import static junit.framework.Assert.*;

@RunWith(RobolectricGradleTestRunner.class)
public class UtilsTest {

    @Test
    public void getNotificationUri() {
        String result = Utils.getNotificationUri(ImprovedTestModel.class).toString();
        assertTrue(result.contains("Tests"));
    }

    @Test
    public void getTableName() {
        assertTrue(Utils.getTableName(ImprovedTestModel.class).equals("Tests"));
    }

    @Test(expected = NoTableAnnotationException.class)
    public void getTableNameNoAnnotation() {
        Utils.getTableName(TestModel.class);
    }

    @Test
    public void insertSqlArgs() {
        String result = Utils.insertSqlArgs("? ?", new Object[]{1, "hej"});
        assertTrue(result.equals("1 'hej'"));
    }

    @Test
    public void getAllDeclaredFields() {
        Field[] fields = Utils.getAllDeclaredFields(ImprovedTestModel.class, Model.class);
        System.out.println(fields[0].getName());
        assertTrue(fields.length == 2);
        assertTrue(fields[0].getName().equals("title"));
        assertTrue(fields[1].getName().equals("id"));
    }

    @Test
    public void concatArrays() {
        String[] result = Utils.concatArrays(new String[]{"1", "2", "3"}, new String[]{"4", "5"});
        assertTrue(Arrays.equals(result, new String[]{"1", "2", "3", "4", "5"}));
    }

}
