package net.messagevortex.asn1.annotator;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import com.google.auto.service.AutoService;
import opennlp.tools.sentdetect.lang.Factory;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({
        // Set of full qualified annotation type names
        
})
@AutoService(Processor.class)
public class AsnOneBuilder extends AbstractProcessor {
  
  @Override
  public synchronized void init(ProcessingEnvironment env){
    // FIXME
  }
  
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> annotataions = new LinkedHashSet<>();
    annotataions.add(Factory.class.getCanonicalName());
    return annotataions;
  }
  
  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
  
  @Override
  public boolean process(Set<? extends TypeElement> annoations, RoundEnvironment env) {
    return true;
  }

}
