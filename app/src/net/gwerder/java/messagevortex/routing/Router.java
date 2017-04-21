package net.gwerder.java.messagevortex.routing;

import net.gwerder.java.messagevortex.accountant.Accountant;
import net.gwerder.java.messagevortex.blending.Blender;
import net.gwerder.java.messagevortex.blending.BlenderListener;

import java.util.List;

/**
 * Created by martin.gwerder on 03.04.2017.
 */
public interface Router extends BlenderListener {

    public boolean addBlendingLayer(Blender blendingLayer);

    public boolean removeBlendingLayer(Blender blendingLayer);

    public List<Blender> getAllBlendingLayer();

    public Accountant setAccountant(Accountant accountant);
    public Accountant getAccountant();

}
