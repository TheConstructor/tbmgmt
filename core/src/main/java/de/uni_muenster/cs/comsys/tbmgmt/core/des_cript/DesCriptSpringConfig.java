package de.uni_muenster.cs.comsys.tbmgmt.core.des_cript;

import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.impl.DesCriptReaderImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.impl.DesCriptWriterImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ExperimentType;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.UtilsSpringConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.InstantFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.Marshaller;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 Created by matthias on 21.05.15.
 */
@Configuration
@Import({UtilsSpringConfig.class})
public class DesCriptSpringConfig {
    public static final Charset DES_CRIPT_ENCODING = StandardCharsets.UTF_8;

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        final Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setContextPath("de.uni_muenster.cs.comsys.tbmgmt.core.schema");
        jaxb2Marshaller.setSchema(resourceLoader.getResource("classpath:schemas/DEScript.xsd"));
        HashMap<String, Object> marshallerProperties = new HashMap<>();
        marshallerProperties.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshallerProperties.put(Marshaller.JAXB_ENCODING, DES_CRIPT_ENCODING.name());
        jaxb2Marshaller.setMarshallerProperties(marshallerProperties);
        jaxb2Marshaller.setMappedClass(ExperimentType.class);
        return jaxb2Marshaller;
    }

    @Bean
    public DesCriptReader desCriptReader(Jaxb2Marshaller jaxb2Marshaller, InstantFormatter instantFormatter,
                                         NodeNameResolver nodeNameResolver,
                                         EvaluationScriptResolver evaluationScriptResolver) {
        return new DesCriptReaderImpl(jaxb2Marshaller, instantFormatter, nodeNameResolver, evaluationScriptResolver);
    }

    @Bean
    public DesCriptWriter desCriptWriter(Jaxb2Marshaller jaxb2Marshaller, InstantFormatter instantFormatter) {
        return new DesCriptWriterImpl(jaxb2Marshaller, instantFormatter);
    }
}
