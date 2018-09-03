package clearcontrol.microscope.lightsheet.state.spatial;

import java.util.ArrayList;

import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerInterface;

/**
 * The PositionListContainer stores stage positions of the microscope, e.g. for
 * path planning
 *
 * Author: @haesleinhuepf 05 2018
 */
public class PositionListContainer extends ArrayList<Position>
                                   implements DataContainerInterface
{

  private final long mTimepoint;

  public PositionListContainer(long pTimepoint)
  {
    mTimepoint = pTimepoint;
  }

  @Override
  public long getTimepoint()
  {
    return mTimepoint;
  }

  @Override
  public boolean isDataComplete()
  {
    return true;
  }

  @Override
  public void dispose()
  {
    clear();
  }

  public Variable<String> getAsStringVariable()
  {
    Variable<String> variable = new Variable<String>("Positions", "")
    {
      @Override
      public String get()
      {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < size(); i++)
        {
          Position position = PositionListContainer.this.get(i);
          result.append(position.mX);
          result.append(",");
          result.append(position.mY);
          result.append(",");
          result.append(position.mZ);
          result.append(";");
        }
        return result.toString();
      }

      @Override
      public void set(String value)
      {
        String[] lines = value.split(";");
        for (String line : lines)
        {
          String[] columns = line.split(",");
          if (columns.length == 3)
          {
            Position position =
                              new Position(Double.parseDouble(columns[0]),
                                           Double.parseDouble(columns[1]),
                                           Double.parseDouble(columns[2]));
            add(position);
          }
        }
      }
    };

    return variable;
  }
}
