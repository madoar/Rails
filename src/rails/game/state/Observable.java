package rails.game.state;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
/**
 * Requirement:
 * The observable object has to call each observer per update() if the object has changed.
 */
public abstract class Observable implements Item {

    // fields for Item implementation
    private final String id;
    private final Item parent;
    private final Context context;
    
    /**
     * @param parent parent node in item hierarchy (cannot be null)
     * @param id id of the observable
     * If id is null it creates an "unobservable" observable
     * This is required for the creation of states that are themselves stateless
     */

    protected Observable(Item parent, String id) {
        checkNotNull(parent, "Parent cannot be null");
        
        // defined standard fields
        this.parent = parent;
        this.id = id;

        if (parent instanceof Context) {
            context = (Context)parent;
        } else { 
            // recursive definition
            context = parent.getContext();
        }

        // if id is null this is an "unobservable" observable
        if (id == null) {
        } else {
            // add item to context if it has an id
            context.addItem(this);
        }
        
    }

    // has to be delayed as at the time of initialization the complete link is not yet defined
    protected StateManager getStateManager() {
        return context.getRoot().getStateManager();
    }

    public void addObserver(Observer o) {
        checkState(id != null, "Cannot add observer to unobservable object");
        getStateManager().addObserver(o, this);
    }
    
    public boolean removeObserver(Observer o) {
        checkState(id != null, "Cannot remove observer from unobservable object");
        return getStateManager().removeObserver(o, this);
    }
    
    public ImmutableSet<Observer> getObservers() {
        checkState(id != null, "Cannot get observers of unobservable object");
        return getStateManager().getObservers(this);
    }

    public void addModel(Model m) {
        checkState(id != null, "Cannot add model to unobservable object");
        getStateManager().addModel(m, this);
    }
    
    public boolean removeModel(Model m) {
        checkState(id != null, "Cannot remove model from unobservable object");
        return getStateManager().removeModel(m, this);
    }
    
    public ImmutableSet<Model> getModels() {
        checkState(id != null, "Cannot get models of unobservable object");
        return getStateManager().getModels(this);
    }
    
    /**
     * Calls update() of registered Models
     * Returns without error for unobservable models
     */
    public void updateModels() {
        if (id == null) return;
        for (Model m:this.getModels()) {
            m.update();
        }
    }
    
    public boolean isObservable() {
        return (id != null);
    }
    
    // Item methods
    
    public String getId() {
        return id;
    }

    public Item getParent() {
        return parent;
    }

    public Context getContext() {
        return context;
    }
    
    public Root getRoot() {
        // forward it to the context
        return context.getRoot();
    }

    public String getURI() {
        checkState(id != null, "Cannot get URI of unobservable object");
        if (parent instanceof Context) {
            return id;
        } else {
            // recursive definition
            return parent.getURI() + Item.SEP + id;
        }
    }
    
    public String getFullURI() {
        checkState(id != null, "Cannot get fullURI of unobservable object");
        // recursive definition
        return parent.getFullURI() + Item.SEP + id;
    }

    
    /**
     * @return text delivered to observers
     */
    public abstract String toText();
    
    @Override
    public String toString() {
        if (id == null) {
            if (parent instanceof Root) {
                return "hidden " + Objects.toStringHelper(this).add("parent", "root");
            } else {
                return "hidden " + Objects.toStringHelper(this).add("parent", parent.getFullURI());
            }
        }
        return Objects.toStringHelper(this).add("uri", getFullURI()).toString();
    }
    
}