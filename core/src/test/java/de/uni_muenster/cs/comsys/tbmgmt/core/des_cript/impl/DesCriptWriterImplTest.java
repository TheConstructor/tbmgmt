package de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptSpringConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.EvaluationScriptResolver;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.EvaluationScriptResolverMock;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.NameResolverMock;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.NodeNameResolver;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.InstantFormatter;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.xml.transform.ResourceSource;
import org.springframework.xml.transform.StringResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import java.util.logging.Logger;

/**
 Created by matthias on 29.03.15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DesCriptWriterImplTest.class, DesCriptSpringConfig.class})
@Rollback
public class DesCriptWriterImplTest {

    private static final Logger LOG = Logger.getLogger(DesCriptWriterImplTest.class.getName());

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    private InstantFormatter instantFormatter;

    @Bean
    public NodeNameResolver nodeNameResolver() {
        return new NameResolverMock();
    }

    @Bean
    public EvaluationScriptResolver evaluationScriptResolver() {
        return new EvaluationScriptResolverMock();
    }

    @Test
    public void testWrite() throws Exception {
        Resource resource = resourceLoader.getResource(
                "classpath:de/uni_muenster/cs/comsys/tbmgmt/core/des_cript/impl/parameter-stepwise.xml");
        Source source = new ResourceSource(resource);

        DesCriptReaderImpl reader = new DesCriptReaderImpl(jaxb2Marshaller, instantFormatter, new NameResolverMock(),
                new EvaluationScriptResolverMock());
        Experiment experiment = reader.read(source);

        DesCriptWriterImpl writer = new DesCriptWriterImpl(jaxb2Marshaller, instantFormatter);
        StringResult result = new StringResult();
        writer.write(experiment, result);


        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        Resource reference = resourceLoader.getResource(
                "classpath:de/uni_muenster/cs/comsys/tbmgmt/core/des_cript/impl/writer-test.xml");
        Document controlDocument = XMLUnit.buildControlDocument(new InputSource(reference.getInputStream()));
        Document testDocument = XMLUnit.buildTestDocument(result.toString());
        Diff diff = XMLUnit.compareXML(controlDocument, testDocument);
        XMLAssert.assertXMLIdentical("XML is not equal", diff, true);
    }
}