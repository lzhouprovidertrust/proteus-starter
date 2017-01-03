/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

package experimental.cms.dsl

import net.proteusframework.cms.component.ContentElement
import net.proteusframework.cms.component.editor.DefaultDelegatePurpose
import net.proteusframework.cms.component.editor.DelegatePurpose

internal fun createContentIdPredicate(existingId: String): (Content) -> Boolean = { it.id == existingId }

interface ContentContainer {
    val contentList: MutableList<Content>
    val contentToRemove: MutableList<Content>
    fun Content.remove() = contentToRemove.add(this)

}
internal fun _getSite(toCheck: Any?): Site {
    return when (toCheck) {
        is Site -> toCheck
        is BoxedContent -> toCheck.site
        is Content -> _getSite(toCheck.parent)
        else -> throw IllegalStateException("Couldn't determine site")
    }
}

interface DelegateContent : ContentContainer {
    val defaultPurpose: DelegatePurpose
    val contentPurpose: MutableMap<Content, DelegatePurpose>
    var parent: Any?
    fun <T : Content> content(content: T, purpose: DelegatePurpose = DefaultDelegatePurpose.NONE, init: T.() -> Unit={}): T {
        contentList.add(content)
        contentPurpose.put(content, purpose)
        content.parent = this
        return content.apply(init)
    }
    fun content(existingContentId: String, purpose: DelegatePurpose = DefaultDelegatePurpose.NONE): Content {
        val contentById = _getSite(parent).getContentById(existingContentId)
        contentList.add(contentById)
        contentPurpose.put(contentById, purpose)
        return contentById
    }
}

interface Content : HTMLIdentifier, HTMLClass, ResourceCapable, PathCapable {
    val id: String
    var parent: Any?

    fun getSite(): Site {
        return _getSite(parent)
    }

    fun createInstance(helper: ContentHelper): ContentElement
    fun isModified(helper: ContentHelper, contentElement: ContentElement): Boolean = false
}

