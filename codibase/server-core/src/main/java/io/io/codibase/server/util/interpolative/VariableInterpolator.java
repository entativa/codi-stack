package io.codibase.server.util.interpolative;

import static io.codibase.k8shelper.KubernetesHelper.ATTRIBUTES;
import static io.codibase.k8shelper.KubernetesHelper.PAUSE;
import static io.codibase.k8shelper.KubernetesHelper.PLACEHOLDER_PREFIX;
import static io.codibase.k8shelper.KubernetesHelper.PLACEHOLDER_SUFFIX;
import static io.codibase.k8shelper.KubernetesHelper.WORKSPACE;
import static io.codibase.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import io.codibase.commons.utils.ExplicitException;
import io.codibase.commons.utils.StringUtils;
import io.codibase.server.buildspec.job.JobVariable;
import io.codibase.server.buildspec.param.ParamCombination;
import io.codibase.server.buildspec.param.spec.ParamSpec;
import io.codibase.server.buildspecmodel.inputspec.Input;
import io.codibase.server.model.Build;
import io.codibase.server.model.support.build.JobProperty;
import io.codibase.server.util.GroovyUtils;
import io.codibase.server.util.interpolative.Interpolative.Segment;
import io.codibase.server.util.interpolative.Interpolative.Segment.Type;
import io.codibase.server.web.editable.EditableStringTransformer;

public class VariableInterpolator {
	
	public static final String PREFIX_PARAM = "param:"; 
	
	public static final String PREFIX_PROPERTY = "property:";
	
	public static final String PREFIX_SECRET = "secret:";
	
	public static final String PREFIX_SCRIPT = "script:";
	
	public static final String PREFIX_FILE = "file:";
	
	public static final String PREFIX_ATTRIBUTE = "attribute:";
	
	private final EditableStringTransformer beanPropertyTransformer;
	
	private final Function<String, String> variableResolver;
	
	public VariableInterpolator(Build build, ParamCombination paramCombination) {
		this(t -> {
			for (JobVariable var : JobVariable.values()) {
				if (var.name().toLowerCase().equals(t)) {
					String value = var.getValue(build);
					return value != null ? value : "";
				}
			}
			if (t.startsWith(PREFIX_PARAM) || t.startsWith("params:")) {
				String paramName;
				if (t.startsWith(PREFIX_PARAM))
					paramName = t.substring(PREFIX_PARAM.length());
				else
					paramName = t.substring("params:".length());

				for (Entry<String, Input> entry : paramCombination.getParamInputs().entrySet()) {
					if (paramName.equals(entry.getKey())) {
						String paramType = entry.getValue().getType();
						List<String> paramValues = new ArrayList<>();
						for (String value : entry.getValue().getValues()) {
							if (paramType.equals(ParamSpec.SECRET))
								value = build.getJobAuthorizationContext().getSecretValue(value);
							paramValues.add(value);
						}
						return StringUtils.join(paramValues, ",");
					}
				}
				throw new ExplicitException("Undefined param: " + paramName);
			} else if (t.startsWith(PREFIX_PROPERTY) || t.startsWith("properties:")) {
				String propertyName;
				if (t.startsWith(PREFIX_PROPERTY))
					propertyName = t.substring(PREFIX_PROPERTY.length());
				else
					propertyName = t.substring("properties:".length());

				JobProperty property = build.getSpec().getPropertyMap().get(propertyName);
				if (property != null) {
					return property.getValue();
				} else {
					for (var projectProperty : build.getProject().getHierarchyJobProperties()) {
						if (projectProperty.getName().equals(propertyName))
							return projectProperty.getValue();
					}
					throw new ExplicitException("Undefined property: " + propertyName);
				}
			} else if (t.startsWith(PREFIX_SECRET) || t.startsWith("secrets:")) {
				String secretName;
				if (t.startsWith(PREFIX_SECRET))
					secretName = t.substring(PREFIX_SECRET.length());
				else
					secretName = t.substring("secrets:".length());
				return build.getJobAuthorizationContext().getSecretValue(secretName);
			} else if (t.startsWith(PREFIX_SCRIPT) || t.startsWith("scripts:")) {
				String scriptName;
				if (t.startsWith(PREFIX_SCRIPT))
					scriptName = t.substring(PREFIX_SCRIPT.length());
				else
					scriptName = t.substring("scripts:".length());

				Map<String, Object> context = new HashMap<>();
				context.put("build", build);
				Object result = GroovyUtils.evalScriptByName(scriptName, context);
				if (result != null)
					return result.toString();
				else
					return "";
			} else if (t.startsWith(PREFIX_FILE)) {
				return PLACEHOLDER_PREFIX + WORKSPACE + "/" + t.substring(PREFIX_FILE.length()) + PLACEHOLDER_SUFFIX;
			} else if (t.startsWith(PREFIX_ATTRIBUTE)) {
				return PLACEHOLDER_PREFIX + ATTRIBUTES + "/" + t.substring(PREFIX_ATTRIBUTE.length()) + PLACEHOLDER_SUFFIX;
			} else if (t.equals(PAUSE)) {
				return PLACEHOLDER_PREFIX + PAUSE + PLACEHOLDER_SUFFIX;
			} else {
				throw new ExplicitException("Unrecognized interpolation variable: " + t);
			}
		});
	}
	
	public VariableInterpolator(Function<String, String> variableResolver) {
		this.variableResolver = variableResolver;
		beanPropertyTransformer = new EditableStringTransformer(t -> interpolate(t));
	}

	@Nullable
	public String interpolate(@Nullable String value) {
		if (value != null) {
			Interpolative interpolative = Interpolative.parse(value);
			StringBuilder builder = new StringBuilder();
			for (Segment segment: interpolative.getSegments(null)) {
				if (segment.getType() == Type.LITERAL) {
					builder.append(segment.getContent());
				} else {
					String interpolated = variableResolver.apply(segment.getContent()); 
					if (interpolated != null)
						builder.append(interpolated);
				}
			}
			// Should not return null here even if result is empty in order not to 
			// surprise caller. For instance command step may have empty line in commands
			// and we should not convert them to null after interpolation
			return builder.toString();
		} else {
			return null;
		}
	}
	
	public <T> T interpolateProperties(T object) {
		return beanPropertyTransformer.transformProperties(
				object, 
				io.codibase.server.annotation.Interpolative.class);
	}	
		
	public static String getHelp() {
		return _T("<b>Tips: </b> Type <tt>@</tt> to <a href='https://docs.codibase.io/appendix/job-variables' target='_blank' tabindex='-1'>insert variable</a>. "
			+ "Use <tt>@@</tt> for literal <tt>@</tt>");
	}
}
