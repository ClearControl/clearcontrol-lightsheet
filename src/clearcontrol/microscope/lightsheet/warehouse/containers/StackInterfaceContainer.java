package clearcontrol.microscope.lightsheet.warehouse.containers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.stack.StackInterface;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public abstract class StackInterfaceContainer extends DataContainerBase implements
                                                               DataContainerInterface,
                                                               Map<String, StackInterface>
{
  HashMap<String, StackInterface> mData = new HashMap<>();

  public StackInterfaceContainer(LightSheetMicroscope pLightSheetMicroscope) {
    super(pLightSheetMicroscope);
  }

  @Override public int size()
  {
    return mData.size();
  }

  @Override public boolean isEmpty()
  {
    return mData.isEmpty();
  }

  @Override public boolean containsKey(Object key)
  {
    return mData.containsKey(key);
  }

  @Override public boolean containsValue(Object value)
  {
    return mData.containsKey(value);
  }

  @Override public StackInterface get(Object key)
  {
    return mData.get(key);
  }

  @Override public StackInterface put(String key,
                                      StackInterface value)
  {
    return mData.put(key, value);
  }

  @Override public StackInterface remove(Object key)
  {
    return mData.remove(key);
  }

  @Override public void putAll(@NotNull Map<? extends String, ? extends StackInterface> m)
  {
    mData.putAll(m);
  }

  @Override public void clear()
  {
    mData.clear();
  }

  @NotNull @Override public Set<String> keySet()
  {
    return mData.keySet();
  }

  @NotNull @Override public Collection<StackInterface> values()
  {
    return mData.values();
  }

  @NotNull @Override public Set<Entry<String, StackInterface>> entrySet()
  {
    return mData.entrySet();
  }

  public void dispose() {
    for (String key : keySet()) {
      get(key).free();
    }
    clear();
  }
}