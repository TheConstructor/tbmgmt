package de.uni_muenster.cs.comsys.tbmgmt.core.db.userTypes;

import de.uni_muenster.cs.comsys.tbmgmt.core.model.MacAddress;
import org.hibernate.HibernateException;
import org.hibernate.TypeMismatchException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 Created by matthias on 09.10.15.
 */
public class Inet6UserType implements UserType {
    public static final String TYPE_STRING    = "de.uni_muenster.cs.comsys.tbmgmt.core.db.userTypes.Inet6UserType";
    public static final String PG_TYPE_STRING = "inet";

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.OTHER};
    }

    @Override
    public Class returnedClass() {
        return MacAddress.class;
    }

    @Override
    public boolean equals(final Object x, final Object y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return Objects.hashCode(x);
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final SharedSessionContractImplementor session,
            final Object owner)
            throws HibernateException, SQLException {
        String value = rs.getString(names[0]);
        if (value == null) {
            return null;
        }
        try {
            return Inet6Address.getByName(value);
        } catch (UnknownHostException e) {
            TypeMismatchException ex = new TypeMismatchException(
                    String.format("Could not read %s as Inet6Address", value));
            ex.initCause(e);
            throw ex;
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
        if (!(value instanceof Inet6Address)) {
            throw new TypeMismatchException(String.format("%s is not of type Inet6Address", value));
        }
        PGobject pGobject = new PGobject();
        pGobject.setType(PG_TYPE_STRING);
        pGobject.setValue(((Inet6Address) value).getHostAddress());
        st.setObject(index, pGobject, Types.OTHER);
    }

    @Override
    public Object deepCopy(final Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(final Object value) throws HibernateException {
        return (MacAddress) value;
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return original;
    }
}
