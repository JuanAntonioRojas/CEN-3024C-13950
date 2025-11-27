package com.ims;

import javafx.scene.control.TextField;
import java.util.function.Consumer;

/**
 * This is the "REMOTE CONTROL" class.
 * This is *not* a normal controller. It's a "data bucket" (like a Model) but for *actions* (code) instead of data.

 * Its job is to hold all the 'lambda' functions (the 'on-click' events) for the buttons in the main 'ViewFrame'.

 * This lets the _23_CtrlMainSceneBuilder create *different* remote  controls (one for 'Admins', one for 'Staff')
 *    and just hand the 'right' one to the _32_ViewFrame.
 * The 'ViewFrame' doesn't know or care if it's an 'Admin' remote; it just wires up the buttons it was given.

 * This is a *very* clean way to handle user permissions.
 * @param <T> A "generic type." This lets us use the same
 * "remote control" for a Product table, a User table, etc.
 */
public class _27_CtrlTableActions<T> {

    //  PUBLIC VARIABLES (The "Buttons" on the remote)

    //  A 'Runnable' is just a "piece of code" that takes no parameters and returns nothing. Perfect for a button click.
    //  DATA ACTIONS
    public Runnable onAdd;
    public Runnable onRemove;
    public Runnable onUpdate;
    public Runnable onRefresh;
    public Runnable onLoadCsv;

    //  A 'Consumer' is a "piece of code" that 'takes' one parameter (like the 'TextField' for the "Remove by ID" button).
    public Consumer<TextField> onRemoveById;


    //  NAVIGATION ACTIONS. Buttons for Admin-only "scene-hopping"
    public Runnable onViewProducts;
    public Runnable onViewUsers;
    public Runnable onViewSuppliers;

    /**
     * This is the empty constructor.
     */
    public _27_CtrlTableActions() {}








    //==================================================================================================================
    //  "FLUENT" SETTERS
    //  These are the "button-programming" methods.
    //  They 'return this' so we can "chain" them together, like new _27_CtrlTableActions()._26a_add(...)._26b_remove(...);
    //==================================================================================================================

    public _27_CtrlTableActions<T> _27a_add(Runnable r)        { this.onAdd = r; return this; }
    public _27_CtrlTableActions<T> _27b_remove(Runnable r)     { this.onRemove = r; return this; }
    public _27_CtrlTableActions<T> _27c_update(Runnable r)     { this.onUpdate = r; return this; }
    public _27_CtrlTableActions<T> _27d_refresh(Runnable r)    { this.onRefresh = r; return this; }
    public _27_CtrlTableActions<T> _27e_loadCsv(Runnable r)    { this.onLoadCsv = r; return this; }
    public _27_CtrlTableActions<T> _27f_removeById(Consumer<TextField> c) { this.onRemoveById = c; return this; }

    //  Admin-only setters
    public _27_CtrlTableActions<T> _27g_viewProducts(Runnable r)    { this.onViewProducts = r; return this; }
    public _27_CtrlTableActions<T> _27h_viewUsers(Runnable r)    { this.onViewUsers = r; return this; }
    public _27_CtrlTableActions<T> _27i_viewSuppliers(Runnable r) { this.onViewSuppliers = r; return this; }
}