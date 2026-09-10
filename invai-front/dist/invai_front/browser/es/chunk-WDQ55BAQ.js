import{a as Le,b as Ke}from"./chunk-MCK5JT3F.js";import{b as Me,d as De,g as ze,h as $e,j as Re}from"./chunk-YKAKMDYW.js";import{I as ve,L as Te,N as Se,T as Ve,U as K,V as Ee,aa as ke,ba as Ae,ca as Fe,ha as Be,p as P,q as V}from"./chunk-Y66MR3DC.js";import{B as Ie,F as we,G as Ce,H as Oe,a as J,d as D,e as k,f as A,k as X}from"./chunk-QES7LLTG.js";import{b as _e,c as ge,d as fe,e as ye,f as xe,i as be}from"./chunk-SV67CR7A.js";import{$b as U,Ab as _,Ac as H,Bb as F,Fb as O,Gb as T,Gc as Z,Hb as C,Ib as w,Oc as h,Pb as I,Pc as E,Qa as s,R as Y,Rb as a,S as ee,Ub as pe,V as te,Vb as re,Wb as f,X as z,Xb as y,Za as Q,aa as u,ac as se,ba as d,ca as L,cb as oe,cc as R,dc as g,ec as B,fc as N,gb as le,gc as j,ha as v,hb as ae,ia as ne,ib as c,ic as ce,jc as ue,kc as de,lc as me,ma as $,mc as he,nc as S,oc as q,pb as x,pc as W,ra as ie,xc as M,yb as r,zb as m}from"./chunk-PM3R7XGD.js";var qe=`
    .p-autocomplete {
        display: inline-flex;
    }

    .p-autocomplete-loader {
        position: absolute;
        top: 50%;
        margin-top: -0.5rem;
        inset-inline-end: dt('autocomplete.padding.x');
    }

    .p-autocomplete:has(.p-autocomplete-dropdown) .p-autocomplete-loader {
        inset-inline-end: calc(dt('autocomplete.dropdown.width') + dt('autocomplete.padding.x'));
    }

    .p-autocomplete:has(.p-autocomplete-dropdown) .p-autocomplete-input {
        flex: 1 1 auto;
        width: 1%;
    }

    .p-autocomplete:has(.p-autocomplete-dropdown) .p-autocomplete-input,
    .p-autocomplete:has(.p-autocomplete-dropdown) .p-autocomplete-input-multiple {
        border-start-end-radius: 0;
        border-end-end-radius: 0;
    }

    .p-autocomplete-dropdown {
        cursor: pointer;
        display: inline-flex;
        user-select: none;
        align-items: center;
        justify-content: center;
        overflow: hidden;
        position: relative;
        width: dt('autocomplete.dropdown.width');
        border-start-end-radius: dt('autocomplete.dropdown.border.radius');
        border-end-end-radius: dt('autocomplete.dropdown.border.radius');
        background: dt('autocomplete.dropdown.background');
        border: 1px solid dt('autocomplete.dropdown.border.color');
        border-inline-start: 0 none;
        color: dt('autocomplete.dropdown.color');
        transition:
            background dt('autocomplete.transition.duration'),
            color dt('autocomplete.transition.duration'),
            border-color dt('autocomplete.transition.duration'),
            outline-color dt('autocomplete.transition.duration'),
            box-shadow dt('autocomplete.transition.duration');
        outline-color: transparent;
    }

    .p-autocomplete-dropdown:not(:disabled):hover {
        background: dt('autocomplete.dropdown.hover.background');
        border-color: dt('autocomplete.dropdown.hover.border.color');
        color: dt('autocomplete.dropdown.hover.color');
    }

    .p-autocomplete-dropdown:not(:disabled):active {
        background: dt('autocomplete.dropdown.active.background');
        border-color: dt('autocomplete.dropdown.active.border.color');
        color: dt('autocomplete.dropdown.active.color');
    }

    .p-autocomplete-dropdown:focus-visible {
        box-shadow: dt('autocomplete.dropdown.focus.ring.shadow');
        outline: dt('autocomplete.dropdown.focus.ring.width') dt('autocomplete.dropdown.focus.ring.style') dt('autocomplete.dropdown.focus.ring.color');
        outline-offset: dt('autocomplete.dropdown.focus.ring.offset');
    }

    .p-autocomplete-overlay {
        position: absolute;
        top: 0;
        left: 0;
        background: dt('autocomplete.overlay.background');
        color: dt('autocomplete.overlay.color');
        border: 1px solid dt('autocomplete.overlay.border.color');
        border-radius: dt('autocomplete.overlay.border.radius');
        box-shadow: dt('autocomplete.overlay.shadow');
        min-width: 100%;
    }

    .p-autocomplete-list-container {
        overflow: auto;
    }

    .p-autocomplete-list {
        margin: 0;
        list-style-type: none;
        display: flex;
        flex-direction: column;
        gap: dt('autocomplete.list.gap');
        padding: dt('autocomplete.list.padding');
    }

    .p-autocomplete-option {
        cursor: pointer;
        white-space: nowrap;
        position: relative;
        overflow: hidden;
        display: flex;
        align-items: center;
        padding: dt('autocomplete.option.padding');
        border: 0 none;
        color: dt('autocomplete.option.color');
        background: transparent;
        transition:
            background dt('autocomplete.transition.duration'),
            color dt('autocomplete.transition.duration'),
            border-color dt('autocomplete.transition.duration');
        border-radius: dt('autocomplete.option.border.radius');
    }

    .p-autocomplete-option:not(.p-autocomplete-option-selected):not(.p-disabled).p-focus {
        background: dt('autocomplete.option.focus.background');
        color: dt('autocomplete.option.focus.color');
    }

    .p-autocomplete-option:not(.p-autocomplete-option-selected):not(.p-disabled):hover {
        background: dt('autocomplete.option.focus.background');
        color: dt('autocomplete.option.focus.color');
    }

    .p-autocomplete-option-selected {
        background: dt('autocomplete.option.selected.background');
        color: dt('autocomplete.option.selected.color');
    }

    .p-autocomplete-option-selected.p-focus {
        background: dt('autocomplete.option.selected.focus.background');
        color: dt('autocomplete.option.selected.focus.color');
    }

    .p-autocomplete-option-group {
        margin: 0;
        padding: dt('autocomplete.option.group.padding');
        color: dt('autocomplete.option.group.color');
        background: dt('autocomplete.option.group.background');
        font-weight: dt('autocomplete.option.group.font.weight');
    }

    .p-autocomplete-input-multiple {
        margin: 0;
        list-style-type: none;
        cursor: text;
        overflow: hidden;
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        padding: calc(dt('autocomplete.padding.y') / 2) dt('autocomplete.padding.x');
        gap: calc(dt('autocomplete.padding.y') / 2);
        color: dt('autocomplete.color');
        background: dt('autocomplete.background');
        border: 1px solid dt('autocomplete.border.color');
        border-radius: dt('autocomplete.border.radius');
        width: 100%;
        transition:
            background dt('autocomplete.transition.duration'),
            color dt('autocomplete.transition.duration'),
            border-color dt('autocomplete.transition.duration'),
            outline-color dt('autocomplete.transition.duration'),
            box-shadow dt('autocomplete.transition.duration');
        outline-color: transparent;
        box-shadow: dt('autocomplete.shadow');
    }

    .p-autocomplete-input-multiple.p-disabled {
        opacity: 1;
        background: dt('autocomplete.disabled.background');
        color: dt('autocomplete.disabled.color');
    }

    .p-autocomplete-input-multiple:not(.p-disabled):hover {
        border-color: dt('autocomplete.hover.border.color');
    }

    .p-autocomplete.p-focus .p-autocomplete-input-multiple:not(.p-disabled) {
        border-color: dt('autocomplete.focus.border.color');
        box-shadow: dt('autocomplete.focus.ring.shadow');
        outline: dt('autocomplete.focus.ring.width') dt('autocomplete.focus.ring.style') dt('autocomplete.focus.ring.color');
        outline-offset: dt('autocomplete.focus.ring.offset');
    }

    .p-autocomplete.p-invalid .p-autocomplete-input-multiple {
        border-color: dt('autocomplete.invalid.border.color');
    }

    .p-variant-filled.p-autocomplete-input-multiple {
        background: dt('autocomplete.filled.background');
    }

    .p-autocomplete-input-multiple.p-variant-filled:not(.p-disabled):hover {
        background: dt('autocomplete.filled.hover.background');
    }

    .p-autocomplete.p-focus .p-autocomplete-input-multiple.p-variant-filled:not(.p-disabled) {
        background: dt('autocomplete.filled.focus.background');
    }

    .p-autocomplete-chip.p-chip {
        padding-block-start: calc(dt('autocomplete.padding.y') / 2);
        padding-block-end: calc(dt('autocomplete.padding.y') / 2);
        border-radius: dt('autocomplete.chip.border.radius');
    }

    .p-autocomplete-input-multiple:has(.p-autocomplete-chip) {
        padding-inline-start: calc(dt('autocomplete.padding.y') / 2);
        padding-inline-end: calc(dt('autocomplete.padding.y') / 2);
    }

    .p-autocomplete-chip-item.p-focus .p-autocomplete-chip {
        background: dt('autocomplete.chip.focus.background');
        color: dt('autocomplete.chip.focus.color');
    }

    .p-autocomplete-input-chip {
        flex: 1 1 auto;
        display: inline-flex;
        padding-block-start: calc(dt('autocomplete.padding.y') / 2);
        padding-block-end: calc(dt('autocomplete.padding.y') / 2);
    }

    .p-autocomplete-input-chip input {
        border: 0 none;
        outline: 0 none;
        background: transparent;
        margin: 0;
        padding: 0;
        box-shadow: none;
        border-radius: 0;
        width: 100%;
        font-family: inherit;
        font-feature-settings: inherit;
        font-size: 1rem;
        color: inherit;
    }

    .p-autocomplete-input-chip input::placeholder {
        color: dt('autocomplete.placeholder.color');
    }

    .p-autocomplete.p-invalid .p-autocomplete-input-chip input::placeholder {
        color: dt('autocomplete.invalid.placeholder.color');
    }

    .p-autocomplete-empty-message {
        padding: dt('autocomplete.empty.message.padding');
    }

    .p-autocomplete-fluid {
        display: flex;
    }

    .p-autocomplete-fluid:has(.p-autocomplete-dropdown) .p-autocomplete-input {
        width: 1%;
    }

    .p-autocomplete:has(.p-inputtext-sm) .p-autocomplete-dropdown {
        width: dt('autocomplete.dropdown.sm.width');
    }

    .p-autocomplete:has(.p-inputtext-sm) .p-autocomplete-dropdown .p-icon {
        font-size: dt('form.field.sm.font.size');
        width: dt('form.field.sm.font.size');
        height: dt('form.field.sm.font.size');
    }

    .p-autocomplete:has(.p-inputtext-lg) .p-autocomplete-dropdown {
        width: dt('autocomplete.dropdown.lg.width');
    }

    .p-autocomplete:has(.p-inputtext-lg) .p-autocomplete-dropdown .p-icon {
        font-size: dt('form.field.lg.font.size');
        width: dt('form.field.lg.font.size');
        height: dt('form.field.lg.font.size');
    }

    .p-autocomplete-clear-icon {
        position: absolute;
        top: 50%;
        margin-top: -0.5rem;
        cursor: pointer;
        color: dt('form.field.icon.color');
        inset-inline-end: dt('autocomplete.padding.x');
    }

    .p-autocomplete:has(.p-autocomplete-dropdown) .p-autocomplete-clear-icon {
        inset-inline-end: calc(dt('autocomplete.padding.x') + dt('autocomplete.dropdown.width'));
    }

    .p-autocomplete:has(.p-autocomplete-clear-icon) .p-autocomplete-input {
        padding-inline-end: calc((dt('form.field.padding.x') * 2) + dt('icon.size'));
    }

    .p-inputgroup .p-autocomplete-dropdown {
        border-radius: 0;
    }

    .p-inputgroup > .p-autocomplete:last-child:has(.p-autocomplete-dropdown) > .p-autocomplete-input {
        border-start-end-radius: 0;
        border-end-end-radius: 0;
    }

    .p-inputgroup > .p-autocomplete:last-child .p-autocomplete-dropdown {
        border-start-end-radius: dt('autocomplete.dropdown.border.radius');
        border-end-end-radius: dt('autocomplete.dropdown.border.radius');
    }
`;var Ue=["item"],Ne=["empty"],je=["header"],We=["footer"],Ze=["selecteditem"],Je=["group"],Xe=["loader"],Ye=["removeicon"],et=["loadingicon"],tt=["clearicon"],nt=["dropdownicon"],it=["focusInput"],ot=["multiIn"],lt=["multiContainer"],at=["ddBtn"],pt=["items"],rt=["scroller"],st=["overlay"],ct=i=>({i}),Ge=i=>({$implicit:i}),ut=(i,p,e)=>({removeCallback:i,index:p,class:e}),G=i=>({height:i}),Qe=(i,p)=>({$implicit:i,options:p}),dt=i=>({options:i}),mt=()=>({}),ht=(i,p,e)=>({option:i,i:p,scrollerOptions:e}),_t=(i,p)=>({$implicit:i,index:p});function gt(i,p){if(i&1){let e=w();m(0,"input",18,2),I("input",function(n){u(e);let o=a();return d(o.onInput(n))})("keydown",function(n){u(e);let o=a();return d(o.onKeyDown(n))})("change",function(n){u(e);let o=a();return d(o.onInputChange(n))})("focus",function(n){u(e);let o=a();return d(o.onInputFocus(n))})("blur",function(n){u(e);let o=a();return d(o.onInputBlur(n))})("paste",function(n){u(e);let o=a();return d(o.onInputPaste(n))})("keyup",function(n){u(e);let o=a();return d(o.onInputKeyUp(n))}),_()}if(i&2){let e=a();g(e.cn(e.cx("pcInputText"),e.inputStyleClass)),r("pAutoFocus",e.autofocus)("pt",e.ptm("pcInputText"))("ngStyle",e.inputStyle)("variant",e.$variant())("invalid",e.invalid())("pSize",e.size())("fluid",e.hasFluid)("pInputTextUnstyled",e.unstyled()),x("type",e.type)("value",e.inputValue())("id",e.inputId)("autocomplete",e.autocomplete)("placeholder",e.placeholder)("name",e.name())("minlength",e.minlength())("min",e.min())("max",e.max())("pattern",e.pattern())("size",e.inputSize())("maxlength",e.maxlength())("tabindex",e.$disabled()?-1:e.tabindex)("required",e.required()?"":void 0)("readonly",e.readonly?"":void 0)("disabled",e.$disabled()?"":void 0)("aria-label",e.ariaLabel)("aria-labelledby",e.ariaLabelledBy)("aria-required",e.required())("aria-expanded",e.overlayVisible??!1)("aria-controls",e.overlayVisible?e.id+"_list":null)("aria-activedescendant",e.focused?e.focusedOptionId:void 0)}}function ft(i,p){if(i&1){let e=w();L(),m(0,"svg",21),I("click",function(){u(e);let n=a(2);return d(n.clear())}),_()}if(i&2){let e=a(2);g(e.cx("clearIcon")),r("pBind",e.ptm("clearIcon")),x("aria-hidden",!0)}}function yt(i,p){}function xt(i,p){i&1&&c(0,yt,0,0,"ng-template")}function bt(i,p){if(i&1){let e=w();m(0,"span",22),I("click",function(){u(e);let n=a(2);return d(n.clear())}),c(1,xt,1,0,null,23),_()}if(i&2){let e=a(2);g(e.cx("clearIcon")),r("pBind",e.ptm("clearIcon")),x("aria-hidden",!0),s(),r("ngTemplateOutlet",e.clearIconTemplate||e._clearIconTemplate)}}function vt(i,p){if(i&1&&(O(0),c(1,ft,1,4,"svg",19)(2,bt,2,5,"span",20),T()),i&2){let e=a();s(),r("ngIf",!e.clearIconTemplate&&!e._clearIconTemplate),s(),r("ngIf",e.clearIconTemplate||e._clearIconTemplate)}}function It(i,p){i&1&&C(0)}function wt(i,p){if(i&1){let e=w();m(0,"span",22),I("click",function(n){u(e);let o=a(2).index,l=a(2);return d(!l.readonly&&!l.$disabled()?l.removeOption(n,o):"")}),L(),F(1,"svg",31),_()}if(i&2){let e=a(4);g(e.cx("chipIcon")),r("pBind",e.ptm("chipIcon")),s(),g(e.cx("chipIcon")),x("aria-hidden",!0)}}function Ct(i,p){}function Ot(i,p){i&1&&c(0,Ct,0,0,"ng-template")}function Tt(i,p){if(i&1&&(m(0,"span",32),c(1,Ot,1,0,null,29),_()),i&2){let e=a(2).index,t=a(2);r("pBind",t.ptm("chipIcon")),x("aria-hidden",!0),s(),r("ngTemplateOutlet",t.removeIconTemplate||t._removeIconTemplate)("ngTemplateOutletContext",W(4,ut,t.removeOption.bind(t),e,t.cx("chipIcon")))}}function St(i,p){if(i&1&&c(0,wt,2,6,"span",20)(1,Tt,2,8,"span",30),i&2){let e=a(3);r("ngIf",!e.removeIconTemplate&&!e._removeIconTemplate),s(),r("ngIf",e.removeIconTemplate||e._removeIconTemplate)}}function Vt(i,p){if(i&1){let e=w();m(0,"li",26,5)(2,"p-chip",28),I("onRemove",function(n){let o=u(e).index,l=a(2);return d(l.readonly?"":l.removeOption(n,o))}),c(3,It,1,0,"ng-container",29)(4,St,2,2,"ng-template",null,6,M),_()()}if(i&2){let e=p.$implicit,t=p.index,n=a(2);g(n.cx("chipItem",S(17,ct,t))),r("pBind",n.ptm("chipItem")),x("id",n.id+"_multiple_option_"+t)("aria-label",n.getOptionLabel(e))("aria-setsize",n.modelValue().length)("aria-posinset",t+1)("aria-selected",!0),s(2),g(n.cx("pcChip")),r("pt",n.ptm("pcChip"))("label",!n.selectedItemTemplate&&!n._selectedItemTemplate&&n.getOptionLabel(e))("disabled",n.$disabled())("removable",!0)("unstyled",n.unstyled()),s(),r("ngTemplateOutlet",n.selectedItemTemplate||n._selectedItemTemplate)("ngTemplateOutletContext",S(19,Ge,e))}}function Et(i,p){if(i&1){let e=w();m(0,"ul",24,3),I("focus",function(n){u(e);let o=a();return d(o.onMultipleContainerFocus(n))})("blur",function(n){u(e);let o=a();return d(o.onMultipleContainerBlur(n))})("keydown",function(n){u(e);let o=a();return d(o.onMultipleContainerKeyDown(n))}),c(2,Vt,6,21,"li",25),m(3,"li",26)(4,"input",27,4),I("input",function(n){u(e);let o=a();return d(o.onInput(n))})("keydown",function(n){u(e);let o=a();return d(o.onKeyDown(n))})("change",function(n){u(e);let o=a();return d(o.onInputChange(n))})("focus",function(n){u(e);let o=a();return d(o.onInputFocus(n))})("blur",function(n){u(e);let o=a();return d(o.onInputBlur(n))})("paste",function(n){u(e);let o=a();return d(o.onInputPaste(n))})("keyup",function(n){u(e);let o=a();return d(o.onInputKeyUp(n))}),_()()()}if(i&2){let e=a();g(e.cx("inputMultiple")),r("pBind",e.ptm("inputMultiple"))("tabindex",-1),x("data-p",e.inputMultipleDataP)("aria-orientation","horizontal")("aria-activedescendant",e.focused?e.focusedMultipleOptionId:void 0),s(2),r("ngForOf",e.modelValue()),s(),g(e.cx("inputChip")),r("pBind",e.ptm("inputChip")),s(),g(e.cx("pcInputText")),r("pAutoFocus",e.autofocus)("pBind",e.ptm("input"))("ngStyle",e.inputStyle),x("type",e.type)("id",e.inputId)("autocomplete",e.autocomplete)("name",e.name())("minlength",e.minlength())("maxlength",e.maxlength())("size",e.size())("min",e.min())("max",e.max())("pattern",e.pattern())("placeholder",e.$filled()?null:e.placeholder)("tabindex",e.$disabled()?-1:e.tabindex)("required",e.required()?"":void 0)("readonly",e.readonly?"":void 0)("disabled",e.$disabled()?"":void 0)("aria-label",e.ariaLabel)("aria-labelledby",e.ariaLabelledBy)("aria-required",e.required())("aria-expanded",e.overlayVisible??!1)("aria-controls",e.overlayVisible?e.id+"_list":null)("aria-activedescendant",e.focused?e.focusedOptionId:void 0)}}function Mt(i,p){if(i&1&&(L(),F(0,"svg",35)),i&2){let e=a(2);g(e.cx("loader")),r("pBind",e.ptm("loader"))("spin",!0),x("aria-hidden",!0)}}function kt(i,p){}function At(i,p){i&1&&c(0,kt,0,0,"ng-template")}function Lt(i,p){if(i&1&&(m(0,"span",32),c(1,At,1,0,null,23),_()),i&2){let e=a(2);g(e.cx("loader")),r("pBind",e.ptm("loader")),x("aria-hidden",!0),s(),r("ngTemplateOutlet",e.loadingIconTemplate||e._loadingIconTemplate)}}function Ft(i,p){if(i&1&&(O(0),c(1,Mt,1,5,"svg",33)(2,Lt,2,5,"span",34),T()),i&2){let e=a();s(),r("ngIf",!e.loadingIconTemplate&&!e._loadingIconTemplate),s(),r("ngIf",e.loadingIconTemplate||e._loadingIconTemplate)}}function Bt(i,p){if(i&1&&F(0,"span",38),i&2){let e=a(2);r("ngClass",e.dropdownIcon),x("aria-hidden",!0)}}function Dt(i,p){if(i&1&&(L(),F(0,"svg",40)),i&2){let e=a(3);r("pBind",e.ptm("dropdown"))}}function Kt(i,p){}function zt(i,p){i&1&&c(0,Kt,0,0,"ng-template")}function $t(i,p){if(i&1&&(O(0),c(1,Dt,1,1,"svg",39)(2,zt,1,0,null,23),T()),i&2){let e=a(2);s(),r("ngIf",!e.dropdownIconTemplate&&!e._dropdownIconTemplate),s(),r("ngTemplateOutlet",e.dropdownIconTemplate||e._dropdownIconTemplate)}}function Rt(i,p){if(i&1){let e=w();m(0,"button",36,7),I("click",function(n){u(e);let o=a();return d(o.handleDropdownClick(n))}),c(2,Bt,1,2,"span",37)(3,$t,3,2,"ng-container",14),_()}if(i&2){let e=a();g(e.cx("dropdown")),r("pBind",e.ptm("dropdown"))("disabled",e.$disabled()),x("aria-label",e.dropdownAriaLabel)("tabindex",e.tabindex),s(2),r("ngIf",e.dropdownIcon),s(),r("ngIf",!e.dropdownIcon)}}function qt(i,p){i&1&&C(0)}function Ht(i,p){i&1&&C(0)}function Pt(i,p){if(i&1&&c(0,Ht,1,0,"ng-container",29),i&2){let e=p.$implicit,t=p.options;a(2);let n=U(6);r("ngTemplateOutlet",n)("ngTemplateOutletContext",q(2,Qe,e,t))}}function Gt(i,p){i&1&&C(0)}function Qt(i,p){if(i&1&&c(0,Gt,1,0,"ng-container",29),i&2){let e=p.options,t=a(4);r("ngTemplateOutlet",t.loaderTemplate||t._loaderTemplate)("ngTemplateOutletContext",S(2,dt,e))}}function Ut(i,p){i&1&&(O(0),c(1,Qt,1,4,"ng-template",null,10,M),T())}function Nt(i,p){if(i&1){let e=w();m(0,"p-scroller",45,9),I("onLazyLoad",function(n){u(e);let o=a(2);return d(o.onLazyLoad.emit(n))}),c(2,Pt,1,5,"ng-template",null,1,M)(4,Ut,3,0,"ng-container",14),_()}if(i&2){let e=a(2);R(S(10,G,e.scrollHeight)),r("tabindex",-1)("pt",e.ptm("virtualScroller"))("items",e.visibleOptions())("itemSize",e.virtualScrollItemSize)("autoSize",!0)("lazy",e.lazy)("options",e.virtualScrollOptions),s(4),r("ngIf",e.loaderTemplate||e._loaderTemplate)}}function jt(i,p){i&1&&C(0)}function Wt(i,p){if(i&1&&(O(0),c(1,jt,1,0,"ng-container",29),T()),i&2){a();let e=U(6),t=a();s(),r("ngTemplateOutlet",e)("ngTemplateOutletContext",q(3,Qe,t.visibleOptions(),he(2,mt)))}}function Zt(i,p){if(i&1&&(m(0,"span"),B(1),_()),i&2){let e=a(2).$implicit,t=a(3);s(),N(t.getOptionGroupLabel(e.optionGroup))}}function Jt(i,p){i&1&&C(0)}function Xt(i,p){if(i&1&&(O(0),m(1,"li",49),c(2,Zt,2,1,"span",14)(3,Jt,1,0,"ng-container",29),_(),T()),i&2){let e=a(),t=e.$implicit,n=e.index,o=a().options,l=a(2);s(),g(l.cx("optionGroup")),r("pBind",l.ptm("optionGroup"))("ngStyle",S(8,G,o.itemSize+"px")),x("id",l.id+"_"+l.getOptionIndex(n,o)),s(),r("ngIf",!l.groupTemplate),s(),r("ngTemplateOutlet",l.groupTemplate)("ngTemplateOutletContext",S(10,Ge,t.optionGroup))}}function Yt(i,p){if(i&1&&(m(0,"span"),B(1),_()),i&2){let e=a(2).$implicit,t=a(3);s(),N(t.getOptionLabel(e))}}function en(i,p){i&1&&C(0)}function tn(i,p){if(i&1){let e=w();O(0),m(1,"li",50),I("click",function(n){u(e);let o=a().$implicit,l=a(3);return d(l.onOptionSelect(n,o))})("mouseenter",function(n){u(e);let o=a().index,l=a().options,b=a(2);return d(b.onOptionMouseEnter(n,b.getOptionIndex(o,l)))}),c(2,Yt,2,1,"span",14)(3,en,1,0,"ng-container",29),_(),T()}if(i&2){let e=a(),t=e.$implicit,n=e.index,o=a().options,l=a(2);s(),g(l.cx("option",W(15,ht,t,n,o))),r("pBind",l.getPTOptions(t,o,n,"option"))("ngStyle",S(19,G,o.itemSize+"px")),x("id",l.id+"_"+l.getOptionIndex(n,o))("aria-label",l.getOptionLabel(t))("aria-selected",l.isSelected(t))("data-p-selected",l.isSelected(t))("aria-disabled",l.isOptionDisabled(t))("data-p-focused",l.focusedOptionIndex()===l.getOptionIndex(n,o))("aria-setsize",l.ariaSetSize)("aria-posinset",l.getAriaPosInset(l.getOptionIndex(n,o))),s(),r("ngIf",!l.itemTemplate&&!l._itemTemplate),s(),r("ngTemplateOutlet",l.itemTemplate||l._itemTemplate)("ngTemplateOutletContext",q(21,_t,t,o.getOptions?o.getOptions(n):n))}}function nn(i,p){if(i&1&&c(0,Xt,4,12,"ng-container",14)(1,tn,4,24,"ng-container",14),i&2){let e=p.$implicit,t=a(3);r("ngIf",t.isOptionGroup(e)),s(),r("ngIf",!t.isOptionGroup(e))}}function on(i,p){if(i&1&&(O(0),B(1),T()),i&2){let e=a(4);s(),j(" ",e.searchResultMessageText," ")}}function ln(i,p){i&1&&C(0,null,12)}function an(i,p){if(i&1&&(m(0,"li",49),c(1,on,2,1,"ng-container",51)(2,ln,2,0,"ng-container",23),_()),i&2){let e=a().options,t=a(2);g(t.cx("emptyMessage")),r("pBind",t.ptm("emptyMessage"))("ngStyle",S(7,G,e.itemSize+"px")),s(),r("ngIf",!t.emptyTemplate&&!t._emptyTemplate)("ngIfElse",t.empty),s(),r("ngTemplateOutlet",t.emptyTemplate||t._emptyTemplate)}}function pn(i,p){if(i&1&&(m(0,"ul",46,11),c(2,nn,2,2,"ng-template",47)(3,an,3,9,"li",48),_()),i&2){let e=p.$implicit,t=p.options,n=a(2);R(t.contentStyle),g(n.cn(n.cx("list"),t.contentStyleClass)),r("pBind",n.ptm("list")),x("id",n.id+"_list")("aria-label",n.listLabel),s(2),r("ngForOf",e),s(),r("ngIf",!e||e&&e.length===0&&n.showEmptyMessage)}}function rn(i,p){i&1&&C(0)}function sn(i,p){if(i&1&&(m(0,"div",41),c(1,qt,1,0,"ng-container",23),m(2,"div",42),c(3,Nt,5,12,"p-scroller",43)(4,Wt,2,6,"ng-container",14),_(),c(5,pn,4,9,"ng-template",null,8,M)(7,rn,1,0,"ng-container",23),_(),m(8,"span",44),B(9),_()),i&2){let e=a();g(e.cn(e.cx("overlay"),e.panelStyleClass)),r("pBind",e.ptm("overlay"))("ngStyle",e.panelStyle),s(),r("ngTemplateOutlet",e.headerTemplate||e._headerTemplate),s(),g(e.cx("listContainer")),se("max-height",e.virtualScroll?"auto":e.scrollHeight),r("pBind",e.ptm("listContainer"))("tabindex",-1),s(),r("ngIf",e.virtualScroll),s(),r("ngIf",!e.virtualScroll),s(3),r("ngTemplateOutlet",e.footerTemplate||e._footerTemplate),s(2),j(" ",e.selectedMessageText," ")}}var cn=`
${qe}

/* For PrimeNG */
p-autoComplete.ng-invalid.ng-dirty .p-autocomplete-input,
p-autoComplete.ng-invalid.ng-dirty .p-autocomplete-input-multiple,
p-auto-complete.ng-invalid.ng-dirty .p-autocomplete-input,
p-auto-complete.ng-invalid.ng-dirty .p-autocomplete-input-multiple p-autocomplete.ng-invalid.ng-dirty .p-autocomplete-input,
p-autocomplete.ng-invalid.ng-dirty .p-autocomplete-input-multiple {
    border-color: dt('autocomplete.invalid.border.color');
}

p-autoComplete.ng-invalid.ng-dirty .p-autocomplete-input:enabled:focus,
p-autoComplete.ng-invalid.ng-dirty:not(.p-disabled).p-focus .p-autocomplete-input-multiple,
p-auto-complete.ng-invalid.ng-dirty .p-autocomplete-input:enabled:focus,
p-auto-complete.ng-invalid.ng-dirty:not(.p-disabled).p-focus .p-autocomplete-input-multiple,
p-autocomplete.ng-invalid.ng-dirty .p-autocomplete-input:enabled:focus,
p-autocomplete.ng-invalid.ng-dirty:not(.p-disabled).p-focus .p-autocomplete-input-multiple {
    border-color: dt('autocomplete.focus.border.color');
}

p-autoComplete.ng-invalid.ng-dirty .p-autocomplete-input-chip input::placeholder,
p-auto-complete.ng-invalid.ng-dirty .p-autocomplete-input-chip input::placeholder,
p-autocomplete.ng-invalid.ng-dirty .p-autocomplete-input-chip input::placeholder {
    color: dt('autocomplete.invalid.placeholder.color');
}

p-autoComplete.ng-invalid.ng-dirty .p-autocomplete-input::placeholder,
p-auto-complete.ng-invalid.ng-dirty .p-autocomplete-input::placeholder,
p-autocomplete.ng-invalid.ng-dirty .p-autocomplete-input::placeholder {
    color: dt('autocomplete.invalid.placeholder.color');
}
`,un={root:{position:"relative"}},dn={root:({instance:i})=>["p-autocomplete p-component p-inputwrapper",{"p-invalid":i.invalid(),"p-focus":i.focused,"p-inputwrapper-filled":i.$filled(),"p-inputwrapper-focus":i.focused&&!i.$disabled()||i.autofocus||i.overlayVisible,"p-autocomplete-open":i.overlayVisible,"p-autocomplete-clearable":i.showClear&&!i.$disabled(),"p-autocomplete-fluid":i.hasFluid}],pcInputText:"p-autocomplete-input",inputMultiple:({instance:i})=>["p-autocomplete-input-multiple",{"p-disabled":i.$disabled(),"p-variant-filled":i.$variant()==="filled"}],chipItem:({instance:i,i:p})=>["p-autocomplete-chip-item",{"p-focus":i.focusedMultipleOptionIndex()===p}],pcChip:"p-autocomplete-chip",chipIcon:"p-autocomplete-chip-icon",inputChip:"p-autocomplete-input-chip",loader:"p-autocomplete-loader",dropdown:"p-autocomplete-dropdown",overlay:({instance:i})=>["p-autocomplete-overlay p-component-overlay p-component",{"p-input-filled":i.$variant()==="filled","p-ripple-disabled":i.config.ripple()===!1}],listContainer:"p-autocomplete-list-container",list:"p-autocomplete-list",optionGroup:"p-autocomplete-option-group",option:({instance:i,option:p,i:e,scrollerOptions:t})=>({"p-autocomplete-option":!0,"p-autocomplete-option-selected":i.isSelected(p),"p-focus":i.focusedOptionIndex()===i.getOptionIndex(e,t),"p-disabled":i.isOptionDisabled(p)}),emptyMessage:"p-autocomplete-empty-message",clearIcon:"p-autocomplete-clear-icon"},He=(()=>{class i extends Te{name="autocomplete";style=cn;classes=dn;inlineStyles=un;static \u0275fac=(()=>{let e;return function(n){return(e||(e=ie(i)))(n||i)}})();static \u0275prov=ee({token:i,factory:i.\u0275fac})}return i})();var Pe=new te("AUTOCOMPLETE_INSTANCE"),mn={provide:Be,useExisting:Y(()=>hn),multi:!0},hn=(()=>{class i extends Re{overlayService;zone;componentName="AutoComplete";$pcAutoComplete=z(Pe,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=z(K,{self:!0});minLength=1;minQueryLength;delay=300;panelStyle;styleClass;panelStyleClass;inputStyle;inputId;inputStyleClass;placeholder;readonly;scrollHeight="200px";lazy=!1;virtualScroll;virtualScrollItemSize;virtualScrollOptions;autoHighlight;forceSelection;type="text";autoZIndex=!0;baseZIndex=0;ariaLabel;dropdownAriaLabel;ariaLabelledBy;dropdownIcon;unique=!0;group;completeOnFocus=!1;showClear=!1;dropdown;showEmptyMessage=!0;dropdownMode="blank";multiple;addOnTab=!1;tabindex;dataKey;emptyMessage;showTransitionOptions=".12s cubic-bezier(0, 0, 0.2, 1)";hideTransitionOptions=".1s linear";autofocus;autocomplete="off";optionGroupChildren="items";optionGroupLabel="label";overlayOptions;get suggestions(){return this._suggestions()}set suggestions(e){this._suggestions.set(e),this.handleSuggestionsChange()}optionLabel;optionValue;id;searchMessage;emptySelectionMessage;selectionMessage;autoOptionFocus=!1;selectOnFocus;searchLocale;optionDisabled;focusOnHover=!0;typeahead=!0;addOnBlur=!1;separator;appendTo=Z(void 0);motionOptions=Z(void 0);completeMethod=new v;onSelect=new v;onUnselect=new v;onAdd=new v;onFocus=new v;onBlur=new v;onDropdownClick=new v;onClear=new v;onInputKeydown=new v;onKeyUp=new v;onShow=new v;onHide=new v;onLazyLoad=new v;inputEL;multiInputEl;multiContainerEL;dropdownButton;itemsViewChild;scroller;overlayViewChild;itemsWrapper;itemTemplate;emptyTemplate;headerTemplate;footerTemplate;selectedItemTemplate;groupTemplate;loaderTemplate;removeIconTemplate;loadingIconTemplate;clearIconTemplate;dropdownIconTemplate;onHostClick(e){this.onContainerClick(e)}value;_suggestions=$(null);timeout;overlayVisible;suggestionsUpdated;highlightOption;highlightOptionChanged;focused=!1;loading;scrollHandler;listId;searchTimeout;dirty=!1;_itemTemplate;_groupTemplate;_selectedItemTemplate;_headerTemplate;_emptyTemplate;_footerTemplate;_loaderTemplate;_removeIconTemplate;_loadingIconTemplate;_clearIconTemplate;_dropdownIconTemplate;focusedMultipleOptionIndex=$(-1);focusedOptionIndex=$(-1);_componentStyle=z(He);$appendTo=H(()=>this.appendTo()||this.config.overlayAppendTo());visibleOptions=H(()=>this.group?this.flatOptions(this._suggestions()):this._suggestions()||[]);inputValue=H(()=>{let e=this.modelValue(),t=this.optionValueSelected?(this.suggestions||[]).find(n=>A(n,e,this.equalityKey())):e;if(D(e))if(typeof e=="object"||this.optionValueSelected){let n=this.getOptionLabel(t);return n??e}else return e;else return""});get focusedMultipleOptionId(){return this.focusedMultipleOptionIndex()!==-1?`${this.id}_multiple_option_${this.focusedMultipleOptionIndex()}`:null}get focusedOptionId(){return this.focusedOptionIndex()!==-1?`${this.id}_${this.focusedOptionIndex()}`:null}get searchResultMessageText(){return D(this.visibleOptions())&&this.overlayVisible?this.searchMessageText.replaceAll("{0}",this.visibleOptions().length):this.emptySearchMessageText}get searchMessageText(){return this.searchMessage||this.config.translation.searchMessage||""}get emptySearchMessageText(){return this.emptyMessage||this.config.translation.emptySearchMessage||""}get selectionMessageText(){return this.selectionMessage||this.config.translation.selectionMessage||""}get emptySelectionMessageText(){return this.emptySelectionMessage||this.config.translation.emptySelectionMessage||""}get selectedMessageText(){return this.hasSelectedOption()?this.selectionMessageText.replaceAll("{0}",this.multiple?this.modelValue()?.length:"1"):this.emptySelectionMessageText}get ariaSetSize(){return this.visibleOptions().filter(e=>!this.isOptionGroup(e)).length}get listLabel(){return this.config.getTranslation(Oe.ARIA).listLabel}get virtualScrollerDisabled(){return!this.virtualScroll}get optionValueSelected(){return typeof this.modelValue()=="string"&&this.optionValue}chipItemClass(e){return this._componentStyle.classes.chipItem({instance:this,i:e})}constructor(e,t){super(),this.overlayService=e,this.zone=t}onInit(){this.id=this.id||ve("pn_id_"),this.cd.detectChanges()}templates;onAfterContentInit(){this.templates.forEach(e=>{switch(e.getType()){case"item":this._itemTemplate=e.template;break;case"group":this._groupTemplate=e.template;break;case"selecteditem":this._selectedItemTemplate=e.template;break;case"selectedItem":this._selectedItemTemplate=e.template;break;case"header":this._headerTemplate=e.template;break;case"empty":this._emptyTemplate=e.template;break;case"footer":this._footerTemplate=e.template;break;case"loader":this._loaderTemplate=e.template;break;case"removetokenicon":this._removeIconTemplate=e.template;break;case"loadingicon":this._loadingIconTemplate=e.template;break;case"clearicon":this._clearIconTemplate=e.template;break;case"dropdownicon":this._dropdownIconTemplate=e.template;break;default:this._itemTemplate=e.template;break}})}onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptms(["host","root"])),this.suggestionsUpdated&&this.overlayViewChild&&this.zone.runOutsideAngular(()=>{setTimeout(()=>{this.overlayViewChild&&this.overlayViewChild.alignOverlay()},1),this.suggestionsUpdated=!1})}handleSuggestionsChange(){if(this.loading){this._suggestions()?.length>0||this.showEmptyMessage||this.emptyTemplate?this.show():this.hide();let e=this.overlayVisible&&this.autoOptionFocus?this.findFirstFocusedOptionIndex():-1;this.focusedOptionIndex.set(e),this.suggestionsUpdated=!0,this.loading=!1,this.cd.markForCheck()}}flatOptions(e){return(e||[]).reduce((t,n,o)=>{t.push({optionGroup:n,group:!0,index:o});let l=this.getOptionGroupChildren(n);return l&&l.forEach(b=>t.push(b)),t},[])}isOptionGroup(e){return this.optionGroupLabel&&e.optionGroup&&e.group}findFirstOptionIndex(){return this.visibleOptions().findIndex(e=>this.isValidOption(e))}findLastOptionIndex(){return X(this.visibleOptions(),e=>this.isValidOption(e))}findFirstFocusedOptionIndex(){let e=this.findSelectedOptionIndex();return e<0?this.findFirstOptionIndex():e}findLastFocusedOptionIndex(){let e=this.findSelectedOptionIndex();return e<0?this.findLastOptionIndex():e}findSelectedOptionIndex(){return this.hasSelectedOption()?this.visibleOptions().findIndex(e=>this.isValidSelectedOption(e)):-1}findNextOptionIndex(e){let t=e<this.visibleOptions().length-1?this.visibleOptions().slice(e+1).findIndex(n=>this.isValidOption(n)):-1;return t>-1?t+e+1:e}findPrevOptionIndex(e){let t=e>0?X(this.visibleOptions().slice(0,e),n=>this.isValidOption(n)):-1;return t>-1?t:e}isValidSelectedOption(e){return this.isValidOption(e)&&this.isSelected(e)}isValidOption(e){return e&&!(this.isOptionDisabled(e)||this.isOptionGroup(e))}isOptionDisabled(e){return this.optionDisabled?k(e,this.optionDisabled):!1}isSelected(e){return this.multiple?this.unique?this.modelValue()?.some(t=>A(t,e,this.equalityKey())):!1:A(this.modelValue(),e,this.equalityKey())}isOptionMatched(e,t){return this.isValidOption(e)&&this.getOptionLabel(e).toLocaleLowerCase(this.searchLocale)===t.toLocaleLowerCase(this.searchLocale)}isInputClicked(e){return e.target===this.inputEL?.nativeElement}isDropdownClicked(e){return this.dropdownButton?.nativeElement?e.target===this.dropdownButton.nativeElement||this.dropdownButton.nativeElement.contains(e.target):!1}equalityKey(){return this.optionValue?void 0:this.dataKey}onContainerClick(e){this.$disabled()||this.loading||this.isInputClicked(e)||this.isDropdownClicked(e)||(!this.overlayViewChild||!this.overlayViewChild.overlayViewChild?.nativeElement.contains(e.target))&&V(this.inputEL?.nativeElement)}handleDropdownClick(e){let t;this.overlayVisible?this.hide(!0):(V(this.inputEL?.nativeElement),t=this.inputEL?.nativeElement?.value,this.dropdownMode==="blank"?this.search(e,"","dropdown"):this.dropdownMode==="current"&&this.search(e,t,"dropdown")),this.onDropdownClick.emit({originalEvent:e,query:t})}onInput(e){if(this.typeahead){let t=this.minQueryLength||this.minLength;this.searchTimeout&&clearTimeout(this.searchTimeout);let n=e.target.value;this.maxlength()!==null&&(n=n.split("").slice(0,this.maxlength()).join("")),!this.multiple&&!this.forceSelection&&this.updateModel(n),n.length===0&&!this.multiple?(this.onClear.emit(),setTimeout(()=>{this.hide()},this.delay/2)):n.length>=t?(this.focusedOptionIndex.set(-1),this.searchTimeout=setTimeout(()=>{this.search(e,n,"input")},this.delay)):this.hide()}}onInputChange(e){this.updateInputWithForceSelection(e)}onInputFocus(e){if(this.$disabled())return;!this.dirty&&this.completeOnFocus&&this.search(e,e.target.value,"focus"),this.dirty=!0,this.focused=!0;let t=this.focusedOptionIndex()!==-1?this.focusedOptionIndex():this.overlayVisible&&this.autoOptionFocus?this.findFirstFocusedOptionIndex():-1;this.focusedOptionIndex.set(t),this.overlayVisible&&this.scrollInView(this.focusedOptionIndex()),this.onFocus.emit(e)}onMultipleContainerFocus(e){this.$disabled()||(this.focused=!0)}onMultipleContainerBlur(e){this.focusedMultipleOptionIndex.set(-1),this.focused=!1}onMultipleContainerKeyDown(e){if(this.$disabled()){e.preventDefault();return}switch(e.code){case"ArrowLeft":this.onArrowLeftKeyOnMultiple(e);break;case"ArrowRight":this.onArrowRightKeyOnMultiple(e);break;case"Backspace":this.onBackspaceKeyOnMultiple(e);break;default:break}}onInputBlur(e){if(this.dirty=!1,this.focused=!1,this.focusedOptionIndex.set(-1),this.addOnBlur&&this.multiple&&!this.typeahead){let t=(this.multiInputEl?.nativeElement?.value||e.target.value||"").trim();t&&!this.isSelected(t)&&(this.updateModel([...this.modelValue()||[],t]),this.onAdd.emit({originalEvent:e,value:t}),this.multiInputEl?.nativeElement?this.multiInputEl.nativeElement.value="":e.target.value="")}this.onModelTouched(),this.onBlur.emit(e)}onInputPaste(e){if(this.separator&&this.multiple&&!this.typeahead){let t=(e.clipboardData||window.clipboardData)?.getData("Text");if(t){let n=t.split(this.separator),o=[...this.modelValue()||[]];if(n.forEach(l=>{let b=l.trim();b&&!this.isSelected(b)&&o.push(b)}),o.length>(this.modelValue()||[]).length){let l=o.slice((this.modelValue()||[]).length);this.updateModel(o),l.forEach(b=>{this.onAdd.emit({originalEvent:e,value:b})}),this.multiInputEl?.nativeElement?this.multiInputEl.nativeElement.value="":e.target.value="",e.preventDefault()}}}else this.onKeyDown(e)}onInputKeyUp(e){this.onKeyUp.emit(e)}onKeyDown(e){if(this.$disabled()){e.preventDefault();return}switch(this.onInputKeydown.emit(e),e.code){case"ArrowDown":this.onArrowDownKey(e);break;case"ArrowUp":this.onArrowUpKey(e);break;case"ArrowLeft":this.onArrowLeftKey(e);break;case"ArrowRight":this.onArrowRightKey(e);break;case"Home":this.onHomeKey(e);break;case"End":this.onEndKey(e);break;case"PageDown":this.onPageDownKey(e);break;case"PageUp":this.onPageUpKey(e);break;case"Enter":case"NumpadEnter":this.onEnterKey(e);break;case"Escape":this.onEscapeKey(e);break;case"Tab":this.onTabKey(e);break;case"Backspace":this.onBackspaceKey(e);break;case"ShiftLeft":case"ShiftRight":break;default:this.handleSeparatorKey(e);break}}handleSeparatorKey(e){if(this.separator&&this.multiple&&!this.typeahead&&(this.separator===e.key||typeof this.separator=="string"&&e.key===this.separator||this.separator instanceof RegExp&&e.key.match(this.separator))){let t=(this.multiInputEl?.nativeElement?.value||e.target.value||"").trim();t&&!this.isSelected(t)&&(this.updateModel([...this.modelValue()||[],t]),this.onAdd.emit({originalEvent:e,value:t}),this.multiInputEl?.nativeElement?this.multiInputEl.nativeElement.value="":e.target.value="",e.preventDefault())}}onArrowDownKey(e){if(!this.overlayVisible)return;let t=this.focusedOptionIndex()!==-1?this.findNextOptionIndex(this.focusedOptionIndex()):this.findFirstFocusedOptionIndex();this.changeFocusedOptionIndex(e,t),e.preventDefault(),e.stopPropagation()}onArrowUpKey(e){if(this.overlayVisible)if(e.altKey)this.focusedOptionIndex()!==-1&&this.onOptionSelect(e,this.visibleOptions()[this.focusedOptionIndex()]),this.overlayVisible&&this.hide(),e.preventDefault();else{let t=this.focusedOptionIndex()!==-1?this.findPrevOptionIndex(this.focusedOptionIndex()):this.findLastFocusedOptionIndex();this.changeFocusedOptionIndex(e,t),e.preventDefault(),e.stopPropagation()}}onArrowLeftKey(e){let t=e.currentTarget;this.focusedOptionIndex.set(-1),this.multiple&&(J(t.value)&&this.hasSelectedOption()?(V(this.multiContainerEL?.nativeElement),this.focusedMultipleOptionIndex.set(this.modelValue().length)):e.stopPropagation())}onArrowRightKey(e){this.focusedOptionIndex.set(-1),this.multiple&&e.stopPropagation()}onHomeKey(e){let{currentTarget:t}=e,n=t.value.length;t.setSelectionRange(0,e.shiftKey?n:0),this.focusedOptionIndex.set(-1),e.preventDefault()}onEndKey(e){let{currentTarget:t}=e,n=t.value.length;t.setSelectionRange(e.shiftKey?0:n,n),this.focusedOptionIndex.set(-1),e.preventDefault()}onPageDownKey(e){this.scrollInView(this.visibleOptions().length-1),e.preventDefault()}onPageUpKey(e){this.scrollInView(0),e.preventDefault()}onEnterKey(e){if(!this.typeahead&&!this.forceSelection&&this.multiple){let t=e.target.value?.trim();t&&!this.isSelected(t)&&(this.updateModel([...this.modelValue()||[],t]),this.onAdd.emit({originalEvent:e,value:t}),this.inputEL?.nativeElement&&(this.inputEL.nativeElement.value=""))}if(this.overlayVisible)this.focusedOptionIndex()!==-1&&this.onOptionSelect(e,this.visibleOptions()[this.focusedOptionIndex()]),this.hide();else return;e.preventDefault()}onEscapeKey(e){this.overlayVisible&&this.hide(!0),e.preventDefault()}onTabKey(e){if(this.focusedOptionIndex()!==-1){this.onOptionSelect(e,this.visibleOptions()[this.focusedOptionIndex()]);return}if(this.multiple&&!this.typeahead){let t=(this.multiInputEl?.nativeElement?.value||this.inputEL?.nativeElement?.value||"").trim();if(this.addOnTab&&t&&!this.isSelected(t)){this.updateModel([...this.modelValue()||[],t]),this.onAdd.emit({originalEvent:e,value:t}),this.multiInputEl?.nativeElement?this.multiInputEl.nativeElement.value="":this.inputEL?.nativeElement&&(this.inputEL.nativeElement.value=""),this.updateInputValue(),e.preventDefault(),this.overlayVisible&&this.hide();return}}this.overlayVisible&&this.hide()}onBackspaceKey(e){if(this.multiple){if(D(this.modelValue())&&!this.inputEL?.nativeElement?.value){let t=this.modelValue()[this.modelValue().length-1],n=this.modelValue().slice(0,-1);this.updateModel(n),this.onUnselect.emit({originalEvent:e,value:t})}e.stopPropagation()}}onArrowLeftKeyOnMultiple(e){let t=this.focusedMultipleOptionIndex()<1?0:this.focusedMultipleOptionIndex()-1;this.focusedMultipleOptionIndex.set(t)}onArrowRightKeyOnMultiple(e){let t=this.focusedMultipleOptionIndex();t++,this.focusedMultipleOptionIndex.set(t),t>this.modelValue().length-1&&(this.focusedMultipleOptionIndex.set(-1),V(this.inputEL?.nativeElement))}onBackspaceKeyOnMultiple(e){this.focusedMultipleOptionIndex()!==-1&&this.removeOption(e,this.focusedMultipleOptionIndex())}onOptionSelect(e,t,n=!0){this.multiple?(this.inputEL?.nativeElement&&(this.inputEL.nativeElement.value=""),this.isSelected(t)||this.updateModel([...this.modelValue()||[],t])):this.updateModel(t),this.onSelect.emit({originalEvent:e,value:t}),n&&this.hide(!0)}onOptionMouseEnter(e,t){this.focusOnHover&&this.changeFocusedOptionIndex(e,t)}search(e,t,n){t!=null&&(n==="input"&&t.trim().length===0||(this.loading=!0,this.completeMethod.emit({originalEvent:e,query:t})))}removeOption(e,t){e.stopPropagation();let n=this.modelValue()[t],o=this.modelValue().filter((l,b)=>b!==t);this.updateModel(o),this.onUnselect.emit({originalEvent:e,value:n}),V(this.inputEL?.nativeElement)}updateModel(e){let t=null;e&&(t=this.multiple?e.map(n=>this.getOptionValue(n)):this.getOptionValue(e)),this.value=t,this.writeModelValue(e),this.onModelChange(t),this.updateInputValue(),this.cd.markForCheck()}updateInputValue(){this.inputEL&&this.inputEL.nativeElement&&(this.multiple?this.inputEL.nativeElement.value="":this.inputEL.nativeElement.value=this.inputValue())}updateInputWithForceSelection(e){let t=this.inputEL?.nativeElement;if(!this.forceSelection||this.overlayVisible||!t?.value)return;let n=this.minQueryLength??this.minLength;if(t.value.length<n)return;let o=this.visibleOptions()?.find(l=>this.isOptionMatched(l,t.value));if(!o){t.value="",this.multiple||this.clear();return}o&&!this.isSelected(o)&&this.onOptionSelect(e,o)}autoUpdateModel(){if((this.selectOnFocus||this.autoHighlight)&&this.autoOptionFocus&&!this.hasSelectedOption()){let e=this.findFirstFocusedOptionIndex();this.focusedOptionIndex.set(e),this.onOptionSelect(null,this.visibleOptions()[this.focusedOptionIndex()],!1)}}scrollInView(e=-1){let t=e!==-1?`${this.id}_${e}`:this.focusedOptionId;if(this.itemsViewChild&&this.itemsViewChild.nativeElement){let n=P(this.itemsViewChild.nativeElement,`li[id="${t}"]`);n?n.scrollIntoView&&n.scrollIntoView({block:"nearest",inline:"nearest"}):this.virtualScrollerDisabled||setTimeout(()=>{this.virtualScroll&&this.scroller?.scrollToIndex(e!==-1?e:this.focusedOptionIndex())},0)}}changeFocusedOptionIndex(e,t){this.focusedOptionIndex()!==t&&(this.focusedOptionIndex.set(t),this.scrollInView(),this.selectOnFocus&&this.onOptionSelect(e,this.visibleOptions()[t],!1))}show(e=!1){this.dirty=!0,this.overlayVisible=!0;let t=this.focusedOptionIndex()!==-1?this.focusedOptionIndex():this.autoOptionFocus?this.findFirstFocusedOptionIndex():-1;this.focusedOptionIndex.set(t),e&&V(this.inputEL?.nativeElement),e&&V(this.inputEL?.nativeElement),this.onShow.emit(),this.cd.markForCheck()}hide(e=!1){let t=()=>{this.dirty=e,this.overlayVisible=!1,this.focusedOptionIndex.set(-1),e&&V(this.inputEL?.nativeElement),this.onHide.emit(),this.updateInputWithForceSelection(null),this.cd.markForCheck()};setTimeout(()=>{t()},0)}clear(){this.updateModel(null),this.inputEL?.nativeElement&&(this.inputEL.nativeElement.value=""),this.onClear.emit()}hasSelectedOption(){return D(this.modelValue())}getAriaPosInset(e){return(this.optionGroupLabel?e-this.visibleOptions().slice(0,e).filter(t=>this.isOptionGroup(t)).length:e)+1}getOptionLabel(e){return this.optionLabel?k(e,this.optionLabel):e&&e.label!=null?e.label:e}getOptionValue(e){return this.optionValue?k(e,this.optionValue):e&&e.value!=null?e.value:e}getOptionIndex(e,t){return this.virtualScrollerDisabled?e:t&&t.getItemOptions(e).index}getOptionGroupLabel(e){return this.optionGroupLabel?k(e,this.optionGroupLabel):e&&e.label!=null?e.label:e}getOptionGroupChildren(e){return this.optionGroupChildren?k(e,this.optionGroupChildren):e.items}getPTOptions(e,t,n,o){return this.ptm(o,{context:{option:e,index:this.getOptionIndex(n,t),selected:this.isSelected(e),focused:this.focusedOptionIndex()===this.getOptionIndex(n,t),disabled:this.isOptionDisabled(e)}})}onOverlayBeforeEnter(){if(this.itemsWrapper=P(this.overlayViewChild.overlayViewChild?.nativeElement,this.virtualScroll?'[data-pc-name="virtualscroller"]':'[data-pc-name="pcoverlay"]'),this.virtualScroll&&(this.scroller?.setContentEl(this.itemsViewChild?.nativeElement),this.scroller?.viewInit()),this.visibleOptions()&&this.visibleOptions().length)if(this.virtualScroll){let e=this.modelValue()?this.focusedOptionIndex():-1;e!==-1&&this.scroller?.scrollToIndex(e)}else{let e=P(this.itemsWrapper,'[data-pc-section="option"][data-p-selected="true"]');e&&e.scrollIntoView({block:"nearest",inline:"center"})}}get containerDataP(){return this.cn({fluid:this.hasFluid})}get overlayDataP(){return this.cn({[`overlay-${this.$appendTo()}`]:!0})}get inputMultipleDataP(){return this.cn({invalid:this.invalid(),disabled:this.$disabled(),focus:this.focused,fluid:this.hasFluid,filled:this.$variant()==="filled",empty:!this.$filled(),[this.size()]:this.size()})}writeControlValue(e,t){let n=this.multiple?this.visibleOptions().filter(o=>e?.some(l=>A(l,o,this.equalityKey()))):this.visibleOptions().find(o=>A(e,o,this.equalityKey()));this.value=e,t(J(n)?e:n),this.updateInputValue(),this.cd.markForCheck()}onDestroy(){this.scrollHandler&&(this.scrollHandler.destroy(),this.scrollHandler=null)}static \u0275fac=function(t){return new(t||i)(Q(Ie),Q(ne))};static \u0275cmp=oe({type:i,selectors:[["p-autoComplete"],["p-autocomplete"],["p-auto-complete"]],contentQueries:function(t,n,o){if(t&1&&pe(o,Ue,5)(o,Ne,5)(o,je,5)(o,We,5)(o,Ze,5)(o,Je,5)(o,Xe,5)(o,Ye,5)(o,et,5)(o,tt,5)(o,nt,5)(o,we,4),t&2){let l;f(l=y())&&(n.itemTemplate=l.first),f(l=y())&&(n.emptyTemplate=l.first),f(l=y())&&(n.headerTemplate=l.first),f(l=y())&&(n.footerTemplate=l.first),f(l=y())&&(n.selectedItemTemplate=l.first),f(l=y())&&(n.groupTemplate=l.first),f(l=y())&&(n.loaderTemplate=l.first),f(l=y())&&(n.removeIconTemplate=l.first),f(l=y())&&(n.loadingIconTemplate=l.first),f(l=y())&&(n.clearIconTemplate=l.first),f(l=y())&&(n.dropdownIconTemplate=l.first),f(l=y())&&(n.templates=l)}},viewQuery:function(t,n){if(t&1&&re(it,5)(ot,5)(lt,5)(at,5)(pt,5)(rt,5)(st,5),t&2){let o;f(o=y())&&(n.inputEL=o.first),f(o=y())&&(n.multiInputEl=o.first),f(o=y())&&(n.multiContainerEL=o.first),f(o=y())&&(n.dropdownButton=o.first),f(o=y())&&(n.itemsViewChild=o.first),f(o=y())&&(n.scroller=o.first),f(o=y())&&(n.overlayViewChild=o.first)}},hostVars:5,hostBindings:function(t,n){t&1&&I("click",function(l){return n.onHostClick(l)}),t&2&&(x("data-p",n.containerDataP),R(n.sx("root")),g(n.cn(n.cx("root"),n.styleClass)))},inputs:{minLength:[2,"minLength","minLength",E],minQueryLength:[2,"minQueryLength","minQueryLength",E],delay:[2,"delay","delay",E],panelStyle:"panelStyle",styleClass:"styleClass",panelStyleClass:"panelStyleClass",inputStyle:"inputStyle",inputId:"inputId",inputStyleClass:"inputStyleClass",placeholder:"placeholder",readonly:[2,"readonly","readonly",h],scrollHeight:"scrollHeight",lazy:[2,"lazy","lazy",h],virtualScroll:[2,"virtualScroll","virtualScroll",h],virtualScrollItemSize:[2,"virtualScrollItemSize","virtualScrollItemSize",E],virtualScrollOptions:"virtualScrollOptions",autoHighlight:[2,"autoHighlight","autoHighlight",h],forceSelection:[2,"forceSelection","forceSelection",h],type:"type",autoZIndex:[2,"autoZIndex","autoZIndex",h],baseZIndex:[2,"baseZIndex","baseZIndex",E],ariaLabel:"ariaLabel",dropdownAriaLabel:"dropdownAriaLabel",ariaLabelledBy:"ariaLabelledBy",dropdownIcon:"dropdownIcon",unique:[2,"unique","unique",h],group:[2,"group","group",h],completeOnFocus:[2,"completeOnFocus","completeOnFocus",h],showClear:[2,"showClear","showClear",h],dropdown:[2,"dropdown","dropdown",h],showEmptyMessage:[2,"showEmptyMessage","showEmptyMessage",h],dropdownMode:"dropdownMode",multiple:[2,"multiple","multiple",h],addOnTab:[2,"addOnTab","addOnTab",h],tabindex:[2,"tabindex","tabindex",E],dataKey:"dataKey",emptyMessage:"emptyMessage",showTransitionOptions:"showTransitionOptions",hideTransitionOptions:"hideTransitionOptions",autofocus:[2,"autofocus","autofocus",h],autocomplete:"autocomplete",optionGroupChildren:"optionGroupChildren",optionGroupLabel:"optionGroupLabel",overlayOptions:"overlayOptions",suggestions:"suggestions",optionLabel:"optionLabel",optionValue:"optionValue",id:"id",searchMessage:"searchMessage",emptySelectionMessage:"emptySelectionMessage",selectionMessage:"selectionMessage",autoOptionFocus:[2,"autoOptionFocus","autoOptionFocus",h],selectOnFocus:[2,"selectOnFocus","selectOnFocus",h],searchLocale:[2,"searchLocale","searchLocale",h],optionDisabled:"optionDisabled",focusOnHover:[2,"focusOnHover","focusOnHover",h],typeahead:[2,"typeahead","typeahead",h],addOnBlur:[2,"addOnBlur","addOnBlur",h],separator:"separator",appendTo:[1,"appendTo"],motionOptions:[1,"motionOptions"]},outputs:{completeMethod:"completeMethod",onSelect:"onSelect",onUnselect:"onUnselect",onAdd:"onAdd",onFocus:"onFocus",onBlur:"onBlur",onDropdownClick:"onDropdownClick",onClear:"onClear",onInputKeydown:"onInputKeydown",onKeyUp:"onKeyUp",onShow:"onShow",onHide:"onHide",onLazyLoad:"onLazyLoad"},features:[me([mn,He,{provide:Pe,useExisting:i},{provide:Se,useExisting:i}]),le([K]),ae],decls:9,vars:14,consts:[["overlay",""],["content",""],["focusInput",""],["multiContainer",""],["focusInput","","multiIn",""],["token",""],["removeicon",""],["ddBtn",""],["buildInItems",""],["scroller",""],["loader",""],["items",""],["empty",""],["pInputText","","aria-autocomplete","list","role","combobox",3,"pAutoFocus","pt","class","ngStyle","variant","invalid","pSize","fluid","pInputTextUnstyled","input","keydown","change","focus","blur","paste","keyup",4,"ngIf"],[4,"ngIf"],["role","listbox",3,"pBind","class","tabindex","focus","blur","keydown",4,"ngIf"],["type","button","pRipple","",3,"pBind","class","disabled","click",4,"ngIf"],[3,"visibleChange","onBeforeEnter","onHide","hostAttrSelector","visible","options","target","appendTo","unstyled","pt","motionOptions"],["pInputText","","aria-autocomplete","list","role","combobox",3,"input","keydown","change","focus","blur","paste","keyup","pAutoFocus","pt","ngStyle","variant","invalid","pSize","fluid","pInputTextUnstyled"],["data-p-icon","times",3,"pBind","class","click",4,"ngIf"],[3,"pBind","class","click",4,"ngIf"],["data-p-icon","times",3,"click","pBind"],[3,"click","pBind"],[4,"ngTemplateOutlet"],["role","listbox",3,"focus","blur","keydown","pBind","tabindex"],["role","option",3,"pBind","class",4,"ngFor","ngForOf"],["role","option",3,"pBind"],["role","combobox","aria-autocomplete","list",3,"input","keydown","change","focus","blur","paste","keyup","pAutoFocus","pBind","ngStyle"],[3,"onRemove","pt","label","disabled","removable","unstyled"],[4,"ngTemplateOutlet","ngTemplateOutletContext"],[3,"pBind",4,"ngIf"],["data-p-icon","times-circle"],[3,"pBind"],["data-p-icon","spinner",3,"pBind","class","spin",4,"ngIf"],[3,"pBind","class",4,"ngIf"],["data-p-icon","spinner",3,"pBind","spin"],["type","button","pRipple","",3,"click","pBind","disabled"],[3,"ngClass",4,"ngIf"],[3,"ngClass"],["data-p-icon","chevron-down",3,"pBind",4,"ngIf"],["data-p-icon","chevron-down",3,"pBind"],[3,"pBind","ngStyle"],[3,"pBind","tabindex"],[3,"tabindex","pt","items","style","itemSize","autoSize","lazy","options","onLazyLoad",4,"ngIf"],["role","status","aria-live","polite",1,"p-hidden-accessible"],[3,"onLazyLoad","tabindex","pt","items","itemSize","autoSize","lazy","options"],["role","listbox",3,"pBind"],["ngFor","",3,"ngForOf"],["role","option",3,"pBind","class","ngStyle",4,"ngIf"],["role","option",3,"pBind","ngStyle"],["pRipple","","role","option",3,"click","mouseenter","pBind","ngStyle"],[4,"ngIf","ngIfElse"]],template:function(t,n){if(t&1){let o=w();c(0,gt,2,32,"input",13)(1,vt,3,2,"ng-container",14)(2,Et,7,37,"ul",15)(3,Ft,3,2,"ng-container",14)(4,Rt,4,8,"button",16),m(5,"p-overlay",17,0),de("visibleChange",function(b){return u(o),ue(n.overlayVisible,b)||(n.overlayVisible=b),d(b)}),I("onBeforeEnter",function(){return u(o),d(n.onOverlayBeforeEnter())})("onHide",function(){return u(o),d(n.hide())}),c(7,sn,10,15,"ng-template",null,1,M),_()}t&2&&(r("ngIf",!n.multiple),s(),r("ngIf",n.$filled()&&!n.$disabled()&&n.showClear&&!n.loading),s(),r("ngIf",n.multiple),s(),r("ngIf",n.loading),s(),r("ngIf",n.dropdown),s(),r("hostAttrSelector",n.$attrSelector),ce("visible",n.overlayVisible),r("options",n.overlayOptions)("target","@parent")("appendTo",n.$appendTo())("unstyled",n.unstyled())("pt",n.ptm("pcOverlay"))("motionOptions",n.motionOptions()),x("data-p",n.overlayDataP))},dependencies:[be,_e,ge,fe,xe,ye,ze,De,Fe,$e,Ve,Le,ke,Me,Ke,Ce,Ae,Ee,K],encapsulation:2,changeDetection:0})}return i})();export{hn as a};
/**i18n:85b369cd519586902b7f4f17a6e24b5cb5057fd4d248a08016454ba7f85e36fe*/
