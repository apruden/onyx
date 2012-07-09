package org.obiba.onyx.quartz.editor.widget.attributes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Strings;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.obiba.magma.Attribute;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.wicket.markup.html.table.IColumnProvider;

public class AttributesPanel extends Panel {

  private OnyxEntityList<Attribute> attributes;

  private IModel<Question> questionModel;

  private Dialog attributeEdition;

  public AttributesPanel(String id, IModel<Question> questionModel) {
    super(id);
    this.questionModel = questionModel;
    AjaxLink<Serializable> ajaxAddLink = new AjaxLink<Serializable>("addAttribute", new Model<Serializable>()) {
      @Override
      public void onClick(AjaxRequestTarget target) {
        System.out.println("add click");
      }
    };
    attributes = new OnyxEntityList<Attribute>("attributes", new AttributesDataProvider(),
        new AttributeColumnProvider(), new Model<String>("Attribute"));

    ajaxAddLink.add(new Image("addImage", Images.ADD));
    add(ajaxAddLink);
    add(attributes);
  }

  private class AttributesDataProvider extends SortableDataProvider<Attribute> {

    @Override
    public Iterator<? extends Attribute> iterator(int first, int count) {
      return questionModel.getObject().getAttributes().iterator();
    }

    @Override
    public int size() {
      return questionModel.getObject().getAttributes().size();
    }

    @Override
    public IModel<Attribute> model(final Attribute object) {
      return new LoadableDetachableModel<Attribute>() {
        @Override
        protected Attribute load() {
          return object;
        }
      };
    }
  }

  private class AttributeColumnProvider implements IColumnProvider<Attribute>, Serializable {

    private final List<IColumn<Attribute>> columns = new ArrayList<IColumn<Attribute>>();

    public AttributeColumnProvider() {
      columns.add(new AbstractColumn<Attribute>(new Model<String>("Name")) {
        @Override
        public void populateItem(Item<ICellPopulator<Attribute>> cellItem, String componentId,
            IModel<Attribute> rowModel) {
          Attribute attribute = rowModel.getObject();
          String formattedNS = "";
          if(Strings.isNullOrEmpty(attribute.getNamespace()) == false) {
            formattedNS = "{" + attribute.getNamespace() + "}";
          }
          cellItem.add(
              new Label(componentId, formattedNS + " " + attribute.getName()));
        }
      });

      columns.add(new AbstractColumn<Attribute>(new Model<String>("Value")) {
        @Override
        public void populateItem(Item<ICellPopulator<Attribute>> cellItem, String componentId,
            IModel<Attribute> rowModel) {
          Attribute attribute = rowModel.getObject();
          String formattedLocale = "";
          if(Strings.isNullOrEmpty(attribute.getNamespace()) == false) {
            formattedLocale = "{" + attribute.getLocale().toString() + "}";
          }
          cellItem.add(
              new Label(componentId, formattedLocale + " " + attribute.getValue().getValue()));
        }
      });

      columns.add(new HeaderlessColumn<Attribute>() {
        @Override
        public void populateItem(Item<ICellPopulator<Attribute>> cellItem, String componentId,
            IModel<Attribute> rowModel) {
          cellItem.add(new LinkFragment(componentId, rowModel));
        }
      });
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<Attribute>> getRequiredColumns() {
      return columns;
    }

    @Override
    public List<IColumn<Attribute>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<Attribute>> getAdditionalColumns() {
      return null;
    }
  }

  private class LinkFragment extends Fragment {

    public LinkFragment(String id, IModel<Attribute> model) {
      super(id, "linkFragment", AttributesPanel.this, model);
      AjaxLink<Attribute> ajaxEditLink = new AjaxLink<Attribute>("editAttribute", model) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          System.out.println("click edit");
        }
      };
      ajaxEditLink.add(new Image("editImage", Images.EDIT));
      add(ajaxEditLink);
      AjaxLink<Attribute> ajaxDeleteLink = new AjaxLink<Attribute>("deleteAttribute", model) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          System.out.println("click delete");
        }
      };
      ajaxDeleteLink.add(new Image("deleteImage", Images.DELETE));
      add(ajaxDeleteLink);
    }

  }
}
