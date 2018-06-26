package de.uni_muenster.cs.comsys.tbmgmt.core.db.userTypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.hamcrest.core.IsInstanceOf;
import org.hibernate.usertype.DynamicParameterizedType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Properties;

/**
 * Created by matthias on 31.01.16.
 */
public class JsonbUserTypeTest {

    private Map<String, String> mapTestValues =
            ImmutableMap.<String, String>builder().put("{ \"key\": \"value\" }\n", "{\"key\":\"value\"}").build();

    @Test
    public void testAssembleDisassemble() {
        JsonbUserType jsonNodeInstance = buildJsonbUserType(JsonNode.class);
        JsonbUserType mapInstance = buildJsonbUserType(Map.class);
        for (Map.Entry<String, String> entry : mapTestValues.entrySet()) {
            reassemblingTest(jsonNodeInstance, entry.getKey(), entry.getValue(), JsonNode.class);
            reassemblingTest(jsonNodeInstance, entry.getValue(), entry.getValue(), JsonNode.class);
            reassemblingTest(mapInstance, entry.getKey(), entry.getValue(), Map.class);
            reassemblingTest(mapInstance, entry.getValue(), entry.getValue(), Map.class);
        }

        reassemblingTest(jsonNodeInstance, "null", "null", JsonNode.class);
        reasemblingToNullTest(mapInstance, "null");
        reasemblingToNullTest(jsonNodeInstance, null);
        reasemblingToNullTest(mapInstance, null);
    }

    private void reasemblingToNullTest(JsonbUserType jsonbUserType, String input) {
        Object assembled = jsonbUserType.assemble(input, null);
        Assert.assertEquals(input + ": assembled.class", null, assembled);
        Serializable disassembled = jsonbUserType.disassemble(assembled);
        Assert.assertEquals(input + ": JSON-String", null, disassembled);
    }

    private void reassemblingTest(JsonbUserType jsonbUserType, String input, String output, Class<?> returnedClass) {
        Object assembled = jsonbUserType.assemble(input, null);
        Assert.assertThat(input + ": assembled.class", assembled, IsInstanceOf.instanceOf(returnedClass));
        Serializable disassembled = jsonbUserType.disassemble(assembled);
        Assert.assertThat(input + ": disassembled.class", disassembled, IsInstanceOf.instanceOf(String.class));
        Assert.assertEquals(input + ": JSON-String", output, disassembled);
    }

    @Test
    public void testGetSet() throws SQLException {
        JsonbUserType jsonNodeInstance = buildJsonbUserType(JsonNode.class);
        JsonbUserType mapInstance = buildJsonbUserType(Map.class);
        for (Map.Entry<String, String> entry : mapTestValues.entrySet()) {
            accessorTest(jsonNodeInstance, entry.getKey(), entry.getValue(), JsonNode.class);
            accessorTest(jsonNodeInstance, entry.getValue(), entry.getValue(), JsonNode.class);
            accessorTest(mapInstance, entry.getKey(), entry.getValue(), Map.class);
            accessorTest(mapInstance, entry.getValue(), entry.getValue(), Map.class);
        }
    }

    private void accessorTest(JsonbUserType jsonbUserType, String input, String output, Class<?> returnedClass)
            throws SQLException {
        String[] names = {RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(1, 42))};
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.getString(Mockito.eq(names[0]))).thenReturn(input);
        Object object = jsonbUserType.nullSafeGet(resultSet, names, null, null);
        //ensure we mocked enough
        Mockito.verify(resultSet).getString(Mockito.eq(names[0]));
        Mockito.verifyNoMoreInteractions(resultSet);
        Assert.assertThat(input + ": object.class", object, IsInstanceOf.instanceOf(returnedClass));

        int index = RandomUtils.nextInt(0, 42);
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        jsonbUserType.nullSafeSet(preparedStatement, object, index, null);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(preparedStatement).setObject(Mockito.eq(index), captor.capture(), Mockito.eq(Types.OTHER));
        Mockito.verifyNoMoreInteractions(preparedStatement);

        Object dbValue = captor.getValue();
        Assert.assertThat(input + ": dbValue.class", dbValue, IsInstanceOf.instanceOf(PGobject.class));
        PGobject pGobject = (PGobject) dbValue;
        Assert.assertEquals(input + ": dbValue.type", JsonbUserType.PG_TYPE_STRING, pGobject.getType());
        Assert.assertEquals(input + ": dbValue.value", output, pGobject.getValue());
    }

    private JsonbUserType buildJsonbUserType(Class<?> returnedClass) {
        JsonbUserType jsonbUserType = new JsonbUserType();
        Properties properties = new Properties();
        properties.put(DynamicParameterizedType.PARAMETER_TYPE, new ParameterTypeImpl(returnedClass));
        jsonbUserType.setParameterValues(properties);
        return jsonbUserType;
    }

    private static class ParameterTypeImpl implements DynamicParameterizedType.ParameterType {

        private final Class returnedClass;

        private ParameterTypeImpl(Class returnedClass) {
            this.returnedClass = returnedClass;
        }

        @Override
        public Class getReturnedClass() {
            return returnedClass;
        }

        @Override
        public Annotation[] getAnnotationsMethod() {
            return new Annotation[0];
        }

        @Override
        public String getCatalog() {
            return null;
        }

        @Override
        public String getSchema() {
            return null;
        }

        @Override
        public String getTable() {
            return null;
        }

        @Override
        public boolean isPrimaryKey() {
            return false;
        }

        @Override
        public String[] getColumns() {
            return new String[0];
        }
    }
}