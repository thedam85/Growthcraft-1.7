/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 IceDragon200
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package growthcraft.api.cellar.fermenting;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import growthcraft.api.cellar.CellarRegistry;
import growthcraft.api.core.schema.FluidStackSchema;
import growthcraft.api.core.schema.ICommentable;
import growthcraft.api.core.schema.ItemKeySchema;
import growthcraft.api.core.util.JsonConfigDef;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class UserFermentingRecipes extends JsonConfigDef
{
	public static class UserFermentingRecipe implements ICommentable
	{
		public String comment = "";
		public ItemKeySchema item;
		public FluidStackSchema input_fluid;
		public FluidStackSchema output_fluid;
		public int time;

		public UserFermentingRecipe(ItemKeySchema i, FluidStackSchema inp_fluid, FluidStackSchema out_fluid, int t)
		{
			this.item = i;
			this.input_fluid = inp_fluid;
			this.output_fluid = out_fluid;
			this.time = t;
		}

		public UserFermentingRecipe() {}

		@Override
		public String toString()
		{
			return String.format("UserFermentingRecipe(`%s` + `%s` / %d = `%s`)", item, input_fluid, time, output_fluid);
		}

		@Override
		public void setComment(String comm)
		{
			this.comment = comm;
		}

		@Override
		public String getComment()
		{
			return comment;
		}
	}

	protected List<UserFermentingRecipe> defaults = new ArrayList<UserFermentingRecipe>();
	protected UserFermentingRecipe[] recipes;

	public void addDefault(UserFermentingRecipe recipe)
	{
		defaults.add(recipe);
	}

	public void addDefault(ItemKeySchema item, FluidStackSchema inputFluid, FluidStackSchema outputFluid, int time)
	{
		addDefault(new UserFermentingRecipe(item, inputFluid, outputFluid, time));
	}

	public void addDefault(ItemStack stack, FluidStack inputFluid, FluidStack outputFluid, int time)
	{
		addDefault(
			new ItemKeySchema(stack),
			new FluidStackSchema(inputFluid),
			new FluidStackSchema(outputFluid),
			time
		);
	}

	@Override
	protected String getDefault()
	{
		final UserFermentingRecipe[] ary = defaults.toArray(new UserFermentingRecipe[defaults.size()]);
		return gson.toJson(ary, UserFermentingRecipe[].class);
	}

	@Override
	protected void loadFromBuffer(BufferedReader reader)
	{
		this.recipes = gson.fromJson(reader, UserFermentingRecipe[].class);
	}

	private void addFermentingRecipe(UserFermentingRecipe recipe)
	{
		if (recipe == null)
		{
			logger.error("Recipe is invalid!");
			return;
		}

		if (recipe.item == null || !recipe.item.isValid())
		{
			logger.error("Recipe item is invalid! {%s}", recipe);
			return;
		}

		if (recipe.input_fluid == null || !recipe.input_fluid.isValid())
		{
			logger.error("Recipe input_fluid is invalid! {%s}", recipe);
			return;
		}

		if (recipe.output_fluid == null || !recipe.output_fluid.isValid())
		{
			logger.error("Recipe output_fluid is invalid! {%s}", recipe);
			return;
		}

		logger.info("Adding Fermenting Recipe {%s}", recipe);
		for (ItemStack item : recipe.item.getItemStacks())
		{
			CellarRegistry.instance().fermenting().addFermentingRecipe(
				recipe.output_fluid.asFluidStack(),
				recipe.input_fluid.asFluidStack(),
				item,
				recipe.time
			);
		}
	}

	@Override
	public void postInit()
	{
		if (recipes != null)
		{
			logger.info("Registering %d user heat sources.", recipes.length);
			for (UserFermentingRecipe recipe : recipes) addFermentingRecipe(recipe);
		}
	}
}
