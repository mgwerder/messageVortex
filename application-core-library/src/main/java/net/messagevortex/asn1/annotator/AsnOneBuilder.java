package net.messagevortex.asn1.annotator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.LinkedHashSet;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({
    // Set of full qualified annotation type names
})

public class AsnOneBuilder extends AbstractProcessor {


  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    // FIXME
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> annotataions = new LinkedHashSet<>();
    annotataions.add(this.getClass().getCanonicalName());
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
