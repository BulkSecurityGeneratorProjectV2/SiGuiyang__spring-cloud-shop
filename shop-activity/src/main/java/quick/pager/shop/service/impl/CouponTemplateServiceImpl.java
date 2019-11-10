package quick.pager.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import quick.pager.shop.constants.Constants;
import quick.pager.shop.constants.ResponseStatus;
import quick.pager.shop.dto.activity.CouponTemplateDTO;
import quick.pager.shop.mapper.DiscountCouponTemplateMapper;
import quick.pager.shop.model.activity.DiscountCouponTemplate;
import quick.pager.shop.response.Response;
import quick.pager.shop.service.CouponTemplateService;
import quick.pager.shop.utils.BeanCopier;
import quick.pager.shop.utils.CopyOptions;
import quick.pager.shop.utils.DateUtils;

@Service
public class CouponTemplateServiceImpl extends ServiceImpl<DiscountCouponTemplateMapper, DiscountCouponTemplate> implements CouponTemplateService {
    @Override
    public Response list(CouponTemplateDTO dto) {

        QueryWrapper<DiscountCouponTemplate> qw = new QueryWrapper<>();

        if (!StringUtils.isEmpty(dto.getTemplateName())) {
            qw.eq("template_name", dto.getTemplateName());
        }

        if (null != dto.getTemplateType()) {
            qw.eq("template_type", dto.getTemplateType());
        }

        qw.orderByDesc("id");
        return this.toPage(dto.getPage(), dto.getPageSize(), qw);
    }

    @Override
    public Response modify(CouponTemplateDTO dto) {

        if (null != dto.getTemplateType() && null != dto.getDiscountStrength()) {
            // 如果是折扣券
            if (Constants.CouponType.DISCOUNT.getType() == dto.getTemplateType()) {
                BigDecimal hundred = new BigDecimal("100");

                if (hundred.compareTo(dto.getDiscountStrength()) <= 0) {
                    return new Response(ResponseStatus.Code.FAIL_CODE, "折扣力度不能必须小于100");
                } else if (BigDecimal.ZERO.compareTo(dto.getDiscountStrength()) >= 0) {
                    return new Response(ResponseStatus.Code.FAIL_CODE, "折扣力度必须是整数");
                }

                dto.setDiscountStrength(dto.getDiscountStrength().divide(new BigDecimal("100")));
            }

        }
        DiscountCouponTemplate template = BeanCopier.create(dto, new DiscountCouponTemplate(), CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true))
                .copy();

        if (null == template.getId()) {
            template.setDeleteStatus(Boolean.FALSE);
            template.setCreateTime(DateUtils.dateTime());
            this.save(template);
        } else {
            this.updateById(template);
        }

        return new Response();
    }

    @Override
    public Response<DiscountCouponTemplate> info(Long id) {
        Response<DiscountCouponTemplate> response = new Response<>();
        DiscountCouponTemplate template = this.baseMapper.selectById(id);
        if (ObjectUtils.isEmpty(template)) {
            response.setCode(ResponseStatus.Code.FAIL_CODE);
            response.setMsg("优惠券模板不存在");
            return response;
        }

        if (template.getDeleteStatus()) {
            response.setCode(ResponseStatus.Code.FAIL_CODE);
            response.setMsg("优惠券模板已禁用");
            return response;
        }
        response.setData(template);
        return response;
    }
}
