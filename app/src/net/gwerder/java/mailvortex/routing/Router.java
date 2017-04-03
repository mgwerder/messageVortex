package net.gwerder.java.mailvortex.routing;

import net.gwerder.java.mailvortex.accountant.Accountant;
import net.gwerder.java.mailvortex.blending.Blender;

import java.util.List;

/**
 * Created by martin.gwerder on 03.04.2017.
 */
public interface Router {

    public boolean addBlendingLayer(Blender blendingLayer);

    public boolean removeBlendingLayer(Blender blendingLayer);

    public List<Blender> getAllBlendingLayer();

    public Accountant setAccountant(Accountant accountant);
    public Accountant getAccountant();

}
