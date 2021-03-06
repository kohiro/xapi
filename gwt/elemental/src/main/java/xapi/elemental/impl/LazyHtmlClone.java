package xapi.elemental.impl;

import static xapi.elemental.X_Elemental.toElement;

import javax.inject.Provider;

import xapi.util.api.ConvertsValue;
import xapi.util.impl.ImmutableProvider;
import xapi.util.impl.LazyProvider;
import elemental.dom.Element;

public class LazyHtmlClone <E extends Element> implements Provider<E> {

  private final Provider<E> provider;
  @SuppressWarnings("unchecked" )
  private ConvertsValue<E, E> converter = ConvertsValue.PASS_THRU;

  public LazyHtmlClone(String html) {
    provider = new LazyProvider<E>(() -> toElement(html));
  }

  public LazyHtmlClone(Provider<String> html) {
    provider = new LazyProvider<E>(() -> {
      String val = html.get();
      return val == null ? null : toElement(val);
    });
  }

  protected E init(E element) {
    return converter.convert(element);
  }

  public LazyHtmlClone<E> setInitializer(ConvertsValue<E, E> initializer) {
    converter = initializer;
    return this;
  }

  @SuppressWarnings({
      "rawtypes", "unchecked"
  } )
  public LazyHtmlClone(Element e) {
    provider = new ImmutableProvider(e);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  } )
  public LazyHtmlClone(Element e, String backup) {
    provider = new LazyProvider(e, () -> toElement(backup));
  }

  @Override
  @SuppressWarnings("unchecked")
  public E get() {
    return init((E)provider.get().cloneNode(true));
  }
}
