package de.uni_muenster.cs.comsys.tbmgmt.core.db.userTypes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.HibernateException;
import org.hibernate.TypeMismatchException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import java.util.Properties;

/**
 Created by matthias on 09.10.15.
 */
public class JsonbUserType implements UserType, DynamicParameterizedType {
    public static final String TYPE_STRING    = "de.uni_muenster.cs.comsys.tbmgmt.core.db.userTypes.JsonbUserType";
    public static final String PG_TYPE_STRING = "jsonb";

    private Class<?>     targetClass  = Object.class;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.OTHER};
    }

    @Override
    public Class returnedClass() {
        return targetClass;
    }

    @Override
    public boolean equals(final Object x, final Object y) throws HibernateException {
        return Objects.equals(objectMapper.valueToTree(x), objectMapper.valueToTree(y));
    }

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return Objects.hashCode(objectMapper.valueToTree(x));
    }
    
    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names,
            final SharedSessionContractImplementor session, final Object owner)
            throws HibernateException, SQLException {
        final String value = rs.getString(names[0]);
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value, targetClass);
        } catch (final IOException e) {
            final TypeMismatchException typeMismatchException = new TypeMismatchException(
                    String.format("Could not read %s as %s", value, targetClass));
            typeMismatchException.initCause(e);
            throw typeMismatchException;
        }
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index,
            final SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER, PG_TYPE_STRING);
            return;
        }
        if (!targetClass.isInstance(value)) {
            throw new TypeMismatchException(String.format("%s is not of type %s", value, targetClass));
        }
        final PGobject pGobject = new PGobject();
        pGobject.setType(PG_TYPE_STRING);
        try {
            pGobject.setValue(objectMapper.writeValueAsString(value));
        } catch (final JsonProcessingException e) {
            final TypeMismatchException typeMismatchException = new TypeMismatchException(
                    String.format("Could not write %s as JSON", value));
            typeMismatchException.initCause(e);
            throw typeMismatchException;
        }
        st.setObject(index, pGobject, Types.OTHER);
    }

    @Override
    public Object deepCopy(final Object value) throws HibernateException {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.treeToValue(objectMapper.valueToTree(value), targetClass);
        } catch (final JsonProcessingException e) {
            final TypeMismatchException typeMismatchException = new TypeMismatchException(
                    String.format("Could not clone %s via JSON", value));
            typeMismatchException.initCause(e);
            throw typeMismatchException;
        }
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(final Object value) throws HibernateException {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (final JsonProcessingException e) {
            final TypeMismatchException typeMismatchException = new TypeMismatchException(
                    String.format("Could not write %s as JSON", value));
            typeMismatchException.initCause(e);
            throw typeMismatchException;
        }
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        if (cached == null) {
            return null;
        }
        try {
            return objectMapper.readValue((String) cached, targetClass);
        } catch (final IOException e) {
            final TypeMismatchException typeMismatchException = new TypeMismatchException(
                    String.format("Could not read %s as %s", cached, targetClass));
            typeMismatchException.initCause(e);
            throw typeMismatchException;
        }
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return deepCopy(original);
    }

    @Override
    public void setParameterValues(final Properties parameters) {
        // IMPL NOTE: we can reach this points in 2 distinct cases:
        // 		1) we are passed a ParameterType instance in the incoming Properties - generally
        //			speaking this indicates the annotation-binding case, and the passed ParameterType
        //			represents information about the attribute and annotation
        //		2) we are not passed a ParameterType - generally this indicates a hbm.xml binding case.
        final ParameterType parameterType = (ParameterType) parameters.get(PARAMETER_TYPE);
        if (parameterType == null) {
            throw new IllegalArgumentException("DynamicParameterizedType.ParameterType is expected. "
                    + "If you use hbm.xml-binding, this could be the reason for failure.");
        }

        targetClass = parameterType.getReturnedClass();
        if (!objectMapper.canSerialize(targetClass)) {
            throw new IllegalArgumentException(String.format("%s can not be serialized by Jackson.", targetClass));
        } else if (!objectMapper.canDeserialize(objectMapper.constructType(targetClass))) {
            throw new IllegalArgumentException(String.format("%s can not be deserialized by Jackson.", targetClass));
        }
    }
}
